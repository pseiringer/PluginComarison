import * as vscode from 'vscode';

export class RecentChangesSettings{
    public static readonly settingsSection: string = "recentChanges";

    public static readonly settingDebounceTime: string = "debounceTime";
    public static readonly settingQueueSize: string = "queueSize";
    
    
    public static getDebounceTimeFromSettings(){
        var settings = vscode.workspace.getConfiguration(RecentChangesSettings.settingsSection);
        var size = settings.get<number>(RecentChangesSettings.settingDebounceTime);
        if (size == undefined){
            size = Number(settings.inspect(RecentChangesSettings.settingDebounceTime)?.defaultValue ?? 1);
        }
        return size;
    }
    
    public static getQueueSizeFromSettings(){
        var settings = vscode.workspace.getConfiguration(RecentChangesSettings.settingsSection);
        var size = settings.get<number>(RecentChangesSettings.settingQueueSize);
        if (size == undefined){
            size = Number(settings.inspect(RecentChangesSettings.settingQueueSize)?.defaultValue ?? 1);
        }
        return size;
    }
}

