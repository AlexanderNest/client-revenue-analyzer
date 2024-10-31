package ru.nesterov.controller.response;

import lombok.Data;

@Data
public class BackupResponse {
    private boolean isBackupDone = false;
    private int eventsSaved = 0;
}
