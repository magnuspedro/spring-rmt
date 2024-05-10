package br.com.magnus.config.starter.file.compressor;

public class FileCompressorException extends RuntimeException {
    public FileCompressorException(String message, Exception ex) {
        super(message, ex);
    }
}
