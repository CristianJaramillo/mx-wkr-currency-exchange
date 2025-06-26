package mx.bank.wkr.currency_exchange.infrastructure.messaging;


import mx.bank.wkr.currency_exchange.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import mx.bank.wkr.currency_exchange.domain.resources.Attachment;
import mx.bank.wkr.currency_exchange.domain.resources.CreateProjectAgentResponse;
import mx.bank.wkr.currency_exchange.domain.resources.Project;
import mx.bank.wkr.currency_exchange.infrastructure.messaging.message.CurrencyExchangeTransactionMessage;
import mx.bank.wkr.currency_exchange.infrastructure.persistence.repository.CurrencyExchangeRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
public class CurrencyExchangeTransactionConsumer {

    private final CurrencyExchangeRepository currencyExchangeRepository;
    private final CreateProjectAgentResponse projectResponse;

    @Value("${ai.resource.input}")
    private String inputPath;

    @Value("${ai.resource.output}")
    private String outputPath;

    @Value("${ai.resource.scripts}")
    private String scriptsPath;

    @Autowired
    private EmailService emailService;

    public CurrencyExchangeTransactionConsumer(CurrencyExchangeRepository currencyExchangeRepository) {
        this.currencyExchangeRepository = currencyExchangeRepository;

        // Crear instancia de Project
        Project project = new Project(
                "bancoppel",        // groupId
                "mx-ms-bc-pro-int-bnk-acnt-corp",  // artifactId
                "1.0.0"               // version
        );

        // Crear lista de Attachment
        Attachment attachment1 = new Attachment(
                "Contract",
                "Pro_retrieveCardsByBatch.docx",
                "<base64-encoded-docx>");

        Attachment attachment2 = new Attachment(
                "Project",
                "mx-ms-bc-party-lifecycle-management.zip",
                "<base64-encoded-zip>");

        List<Attachment> attachments = Arrays.asList(attachment1, attachment2);

        // Fecha actual para createdAt y updatedAt
        Date now = new Date();

        // Crear instancia completa de CreateProjectAgentResponse
        this.projectResponse = new CreateProjectAgentResponse(
                "3e259359-bcd1-41f4-826a-56e8f0cbdfe6",
                "NEW",
                "CREATED",
                project,
                attachments,
                now,
                now
        );
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue}")
    public void handle(@Payload CurrencyExchangeTransactionMessage currencyExchangeTransactionMessage) throws IOException {
        log.info("🔁 Evento recibido: {}", currencyExchangeTransactionMessage);
        CreateProjectAgentResponse request = this.projectResponse;
        String type = request.type();

        switch (type.toUpperCase()) {
            case "NEW" ->
                    projectNew(request);
            case "EXIST" ->
                    projectExist(request);
            default -> log.warn("⚠️ Tipo de proyecto desconocido: {}", type);
        }
        processDocx(request);

        //Empaquetado de proyecto
        String id = projectResponse.id();
        Path carpetaProyecto = Paths.get(outputPath, projectResponse.project().artifactId());
        Path outputsRoot = Paths.get(outputPath);  // Raíz de outputs

        comprimirProyecto(id, carpetaProyecto, outputsRoot);
        eliminarDirectorioRecursivo(carpetaProyecto);
    }

    private void projectNew(CreateProjectAgentResponse request){
        log.info("📁 Tipo de proyecto: NUEVO");
        Project project = request.project();
        if (project == null) {
            log.error("❌ No se proporcionó información de 'project' para tipo NEW.");
            return;
        }

        // Construir ruta al .jar
        String jarPath = Paths.get(scriptsPath, "cli-generador-arquetipo.jar").toString();

        log.info("outputPath" + outputPath.toString());
        // Construir el comando
        List<String> command = Arrays.asList(
                "java",
                "-jar",
                jarPath,
                "--groupId=" + project.groupId(),
                "--nombreProyecto=" + project.artifactId(),
                "--version=" + project.version(),
                "--output=" + outputPath.toString()
        );

        log.info("🐍 Comando completo a ejecutar: {}", command);

        log.info("🚀 Ejecutando JAR con comando: {}", String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(scriptsPath)); // opcional: setear working dir
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[cli-generador-arquetipo] {}", line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("✅ Proyecto generado exitosamente.");
            }
            if (exitCode != 0) {
                throw new RuntimeException("El proceso del generador de proyecto falló con código: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            log.error("💥 Falló la ejecución del generador de arquetipo", e);
            Thread.currentThread().interrupt();
        }
    }

    private void projectExist(CreateProjectAgentResponse request){
        log.info("📁 Tipo de proyecto: EXISTENTE");
        // Buscar el Attachment de tipo "Project"
        Optional<Attachment> projectZipAttachment = request.attachments().stream()
                .filter(att -> "Project".equalsIgnoreCase(att.type()))
                .findFirst();

        if (projectZipAttachment.isEmpty()) {
            log.error("❌ No se encontró un Attachment de tipo 'Project' para proyecto EXISTENTE.");
            return;
        }

        Attachment zipAttachment = projectZipAttachment.get();
        String zipFileName = zipAttachment.name();
        String zipBase64Content = zipAttachment.content();

        // Ruta completa donde se guardará temporalmente el ZIP
        Path zipPath = Paths.get(inputPath, zipFileName);

        try {
            // Decodificar contenido base64 y escribir el archivo ZIP
            byte[] zipBytes = Base64.getDecoder().decode(zipBase64Content);
            Files.write(zipPath, zipBytes);
            log.info("📦 Archivo ZIP guardado en: {}", zipPath);

            // Descomprimir en outputPath
            unzip(zipPath.toFile(), new File(outputPath));
            log.info("📂 Proyecto descomprimido en: {}", outputPath);

        } catch (IOException e) {
            log.error("💥 Error al guardar o descomprimir el archivo ZIP", e);
        }
    }

    private void processDocx(CreateProjectAgentResponse request){
        // Paso común: procesar el archivo .docx de tipo "Contract"
        Optional<Attachment> contractAttachment = request.attachments().stream()
                .filter(att -> "Contract".equalsIgnoreCase(att.type()))
                .findFirst();

        if (contractAttachment.isEmpty()) {
            log.error("❌ No se encontró un Attachment de tipo 'Contract'");
            return;
        }

        Attachment docxAttachment = contractAttachment.get();
        String docxFileName = docxAttachment.name();

        // Ruta esperada del archivo en la carpeta de inputs
        Path docxPath = Paths.get(inputPath, projectResponse.id(), docxFileName);
        File docxFile = docxPath.toFile();

        if (!docxFile.exists()) {
            log.error("❌ El archivo .docx '{}' no se encuentra en {}", docxFileName, docxPath.toAbsolutePath());
            return;
        }

        // Construir ruta del script Python
        String scriptPath = Paths.get(scriptsPath, "generador_proyectos.py").toString();

        // Ejecutar script Python con el archivo .docx y carpeta de salida
        List<String> command = Arrays.asList(
                "python", scriptPath,
                "--docx", docxPath.toString(),
                "--output", Paths.get(outputPath, projectResponse.project().artifactId()).toString()
        );

        log.info("⏳ Ejecutando Agente Llama3 (procesando...)");
        long inicio = System.currentTimeMillis(); // ← marca el tiempo de inicio

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(scriptsPath));
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[script-python] {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("✅ Agente Llama3 ejecutado correctamente.");
                Map<String, Object> model = new HashMap<>();
                model.put("title", "¡AI agent procesado con éxito!");
                model.put("description", "El proceso de generación de código por IA fue generado exitosamente.");

                List<String> recipients = List.of(
                        "e_jlarriaga@bancoppel.com",
                        "e_gamaldonado@bancoppel.com",
                        "e_cjaramillo@bancoppel.com",
                        "ilolmos@bancoppel.com",
                        "ramartinezg@bancoppel.com",
                        "respinosam@bancoppel.com"
                );


                String path = outputPath +"/"+projectResponse.id() +"/" + projectResponse.project().artifactId()+".zip";

                emailService.sendEmailWithTemplateAndAttachment(recipients, "Reporte generado", model, path);
            } else {
                log.error("❌ Error al ejecutar Agente Llama3. Código de salida: {}", exitCode);
            }

        } catch (IOException | InterruptedException e) {
            log.error("💥 Error al ejecutar el script Python", e);
            Thread.currentThread().interrupt();
        }
    }

    public void comprimirProyecto(String id, Path carpetaProyecto, Path outputsRoot) throws IOException {
        Path carpetaDestino = outputsRoot.resolve(id);
        Files.createDirectories(carpetaDestino);

        String nombreZip = carpetaProyecto.getFileName().toString() + ".zip";
        Path zipPath = carpetaDestino.resolve(nombreZip);

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Files.walk(carpetaProyecto)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(carpetaProyecto.relativize(path).toString().replace("\\", "/"));
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }

        System.out.println("[ZIP] Proyecto comprimido en: " + zipPath);
    }

    public void eliminarDirectorioRecursivo(Path path) throws IOException {
        if (!Files.exists(path)) return;

        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        System.out.println("[CLEAN] Carpeta eliminada: " + path);
    }

    private void unzip(File zipFile, File targetDir) throws IOException {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    // Asegurar que la carpeta padre exista
                    new File(newFile.getParent()).mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

}