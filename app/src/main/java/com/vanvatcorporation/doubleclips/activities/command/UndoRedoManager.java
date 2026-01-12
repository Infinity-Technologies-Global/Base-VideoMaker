package com.vanvatcorporation.doubleclips.activities.command;

import com.vanvatcorporation.doubleclips.activities.EditingActivity;
import java.util.Stack;

public class UndoRedoManager {
    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();
    private final int maxHistorySize = 50;
    private EditingActivity activity;

    public UndoRedoManager(EditingActivity activity) {
        this.activity = activity;
    }

    public void executeCommand(Command command) {
        command.execute(activity);
        undoStack.push(command);
        if (undoStack.size() > maxHistorySize) {
            undoStack.remove(0);
        }
        redoStack.clear();
    }

    public void addCommand(Command command) {
        undoStack.push(command);
        if (undoStack.size() > maxHistorySize) {
            undoStack.remove(0);
        }
        redoStack.clear();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (!canUndo()) {
            return;
        }
        Command command = undoStack.pop();
        command.undo(activity);
        redoStack.push(command);
    }

    public void redo() {
        if (!canRedo()) {
            return;
        }
        Command command = redoStack.pop();
        command.execute(activity);
        undoStack.push(command);
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}

