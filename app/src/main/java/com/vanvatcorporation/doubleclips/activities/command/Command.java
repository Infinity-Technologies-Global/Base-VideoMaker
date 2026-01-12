package com.vanvatcorporation.doubleclips.activities.command;

import com.vanvatcorporation.doubleclips.activities.EditingActivity;

public interface Command {
    void execute(EditingActivity activity);
    void undo(EditingActivity activity);
    String getDescription();
}

