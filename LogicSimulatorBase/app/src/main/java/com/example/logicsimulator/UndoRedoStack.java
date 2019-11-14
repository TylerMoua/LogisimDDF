package com.example.logicsimulator;

import java.util.Stack;


public class UndoRedoStack extends Stack {

    private Stack undoStack;
    private Stack redoStack;

    public UndoRedoStack() {
        undoStack = new Stack();
        redoStack = new Stack();
    }

    public Node push(Node value) {
        super.push(value);
        undoStack.push("push");
        redoStack.clear();
        return value;
    }


    public Node pop() {
        Node value = (Node) super.pop();
        undoStack.push(value);
        undoStack.push("pop");
        redoStack.clear();
        return value;

    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }


    public void undo() {
        if (!canUndo()) {
            throw new IllegalStateException();
        }
        Object action = undoStack.pop();
        if (action.equals("push")) {
            Node value = (Node) super.pop();
            redoStack.push(value);
            redoStack.push("push");
        } else {
            Node value = (Node) undoStack.pop();
            super.push(value);
            redoStack.push("pop");
        }
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void redo() {
        if (!canRedo()) {
            throw new IllegalStateException();
        }
        Object action = redoStack.pop();
        if (action.equals("push")) {
            Node value = (Node) redoStack.pop();
            super.push(value);
            undoStack.push("push");
        } else {
            Node value = (Node) super.pop();
            undoStack.push(value);
            undoStack.push("pop");
        }
    }



    }


