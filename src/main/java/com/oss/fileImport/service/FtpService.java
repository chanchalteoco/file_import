package com.oss.fileImport.service;

import com.oss.fileImport.exception.FtpException;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FtpService {

    private static final Logger logger = LoggerFactory.getLogger(FtpService.class);

    @Value("${ftp.server.host}")
    private String ftpHost;

    @Value("${ftp.server.port}")
    private int ftpPort;

    @Value("${ftp.server.username}")
    private String ftpUser;

    @Value("${ftp.server.password}")
    private String ftpPassword;

    @Value("${ftp.server.remote-directory}")
    private String remoteDirectory;

    @Value("${ftp.server.local-directory}")
    private String localDirectory;

    public void fetchFiles() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpHost, ftpPort);
            logger.info("Connected to FTP server: {}:{}", ftpHost, ftpPort);

            boolean loginSuccess = ftpClient.login(ftpUser, ftpPassword);
            if (loginSuccess) {
                logger.info("Logged into FTP server with user: {}", ftpUser);
            } else {
                logger.error("Failed to login to FTP server with user: {}", ftpUser);
                return;
            }

            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(remoteDirectory);
            logger.info("Changed working directory to: {}", remoteDirectory);

            String[] files = ftpClient.listNames();
            if (files != null && files.length > 0) {
                logger.info("Found {} files in remote directory.", files.length);
                for (String fileName : files) {
                    File localFile = new File(localDirectory, fileName);
                    try (FileOutputStream fos = new FileOutputStream(localFile)) {
                        boolean retrieved = ftpClient.retrieveFile(fileName, fos);
                        if (retrieved) {
                            logger.info("Successfully retrieved file: {}", fileName);
                        } else {
                            logger.error("Failed to retrieve file: {}", fileName);
                        }
                    }
                }
            } else {
                logger.warn("No files found in remote directory: {}", remoteDirectory);
            }

        } catch (IOException e) {
            logger.error("Failed to fetch files from FTP server", e);
            throw new FtpException("Failed to fetch files from FTP server", e);
        } finally {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
                logger.info("Logged out and disconnected from FTP server.");
            } catch (IOException ignored) {
                logger.error("Failed to logout or disconnect from FTP server", ignored);
            }
        }
    }
}


