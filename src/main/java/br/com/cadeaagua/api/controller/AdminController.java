package br.com.cadeaagua.api.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    @GetMapping("/logs")
    public AdminLogsResponse listarLogs(@RequestParam(defaultValue = "120") int linhas) {
        int limite = Math.max(10, Math.min(linhas, 300));

        return new AdminLogsResponse(
                LocalDateTime.now().toString(),
                List.of(
                        lerArquivoLog("server.out.log", limite),
                        lerArquivoLog("server.err.log", limite)
                )
        );
    }

    private LogFileResponse lerArquivoLog(String nomeArquivo, int limite) {
        Path caminho = Path.of(nomeArquivo);

        if (!Files.exists(caminho)) {
            return new LogFileResponse(nomeArquivo, caminho.toAbsolutePath().toString(), 0, 0, List.of("Arquivo ainda nao encontrado."));
        }

        try {
            List<String> linhas = Files.readAllLines(caminho, StandardCharsets.UTF_8);
            int inicio = Math.max(0, linhas.size() - limite);

            return new LogFileResponse(
                    nomeArquivo,
                    caminho.toAbsolutePath().toString(),
                    Files.size(caminho),
                    linhas.size(),
                    linhas.subList(inicio, linhas.size())
            );
        } catch (IOException e) {
            return new LogFileResponse(nomeArquivo, caminho.toAbsolutePath().toString(), 0, 0, List.of("Erro ao ler log: " + e.getMessage()));
        }
    }

    public record AdminLogsResponse(String atualizadoEm, List<LogFileResponse> arquivos) {
    }

    public record LogFileResponse(String nome, String caminho, long tamanhoBytes, int totalLinhas, List<String> linhas) {
    }
}
