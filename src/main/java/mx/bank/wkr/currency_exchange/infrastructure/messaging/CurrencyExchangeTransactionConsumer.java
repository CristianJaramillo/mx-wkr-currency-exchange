package mx.bank.wkr.currency_exchange.infrastructure.messaging;


import mx.bank.wkr.currency_exchange.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import mx.bank.wkr.currency_exchange.domain.mapper.CurrencyExchangeTransactionMessageMapper;
import mx.bank.wkr.currency_exchange.domain.mapper.CurrencyExchangeTransactionModelMapper;
import mx.bank.wkr.currency_exchange.domain.model.CurrencyExchangeTransactionModel;
import mx.bank.wkr.currency_exchange.domain.resources.Attachment;
import mx.bank.wkr.currency_exchange.domain.resources.CreateProjectAgentResponse;
import mx.bank.wkr.currency_exchange.domain.resources.Project;
import mx.bank.wkr.currency_exchange.infrastructure.messaging.config.RabbitMqProperties;
import mx.bank.wkr.currency_exchange.infrastructure.messaging.message.CurrencyExchangeTransactionMessage;
import mx.bank.wkr.currency_exchange.infrastructure.persistence.entity.CurrencyExchangeTransactionEntity;
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

@Slf4j
@Component
public class CurrencyExchangeTransactionConsumer {

    @Autowired
    private EmailService emailService;

    private final CurrencyExchangeRepository currencyExchangeRepository;
    private final CreateProjectAgentResponse projectResponse;

    @Value("${ai.resource.input}")
    private String inputPath;

    @Value("${ai.resource.output}")
    private String outputPath;

    @Value("${ai.resource.scripts}")
    private String scriptsPath;

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
    public void handle(@Payload CurrencyExchangeTransactionMessage currencyExchangeTransactionMessage) {
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
                "--output", Paths.get(outputPath, projectResponse.project().artifactId(), projectResponse.project().artifactId()).toString()
        );

        log.info("⏳ Ejecutando script Python (procesando...)");
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
                log.info("✅ Script Python ejecutado correctamente.");
                Map<String, Object> model = new HashMap<>();
                model.put("title", "¡I agent Procesado con éxito!");
                model.put("description", "El proceso de generación de código por IA fue generado exitosamente.");

                List<String> recipients = List.of(
                        "e_jlarriaga@bancoppel.com",
                        "e_gamaldonado@bancoppel.com",
                        "e_cjaramillo@bancoppel.com",
                        "ilolmos@bancoppel.com"
                );
                String path = outputPath + "/mx-ms-bc-pro-int-bnk-acnt-corp.zip";

                emailService.sendEmailWithTemplateAndAttachment(recipients, "Reporte generado", model, path);

            } else {
                log.error("❌ Error al ejecutar script Python. Código de salida: {}", exitCode);
            }

        } catch (IOException | InterruptedException e) {
            log.error("💥 Error al ejecutar el script Python", e);
            Thread.currentThread().interrupt();
        }
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