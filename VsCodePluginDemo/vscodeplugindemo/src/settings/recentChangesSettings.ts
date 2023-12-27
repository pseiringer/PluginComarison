import * as vscode from 'vscode';

export class RecentChangesSettings{
    public static readonly settingsSection: string = "recentChanges";

    public static readonly settingDebounceTime: string = "debounceTime";
    public static readonly settingQueueSize: string = "queueSize";
    
    // returns the debounce time (in ms) that is stored in the settings
    public static getDebounceTimeFromSettings(): number {
        return RecentChangesSettings.getNumberFromSettings(
            RecentChangesSettings.settingsSection, 
            RecentChangesSettings.settingDebounceTime, 
            1);
    }
    
    // returns the queue size that is stored in the settings
    public static getQueueSizeFromSettings(): number {
        return RecentChangesSettings.getNumberFromSettings(
            RecentChangesSettings.settingsSection, 
            RecentChangesSettings.settingQueueSize, 
            1);
    }

    private static getNumberFromSettings(section: string, setting: string, fallbackDefault: number): number{
        // get the correct settings section
        var settings = vscode.workspace.getConfiguration(section);
        var value = settings.get<number>(setting);

        // check if a value could be found
        if (value === undefined){
            // the setting was somehow invalid, get the default value
            value = Number(settings.inspect(setting)?.defaultValue ?? fallbackDefault);
        }

        return value;
    }
}

