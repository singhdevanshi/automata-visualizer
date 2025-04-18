package com.devanshi.automatavisualizer.model;

import java.util.HashMap;
import java.util.Map;

public class State {
    private String name;
    private boolean isInitial;
    private boolean isAccepting;
    private Map<Character, State> transitions;

    public State(String name, boolean isInitial, boolean isAccepting) {
        this.name = name;
        this.isInitial = isInitial;
        this.isAccepting = isAccepting;
        this.transitions = new HashMap<>();
    }

    public void addTransition(char symbol, State target) {
        transitions.put(symbol, target);
    }

    public State getTransition(char symbol) {
        return transitions.get(symbol);
    }

    public String getName() {
        return name;
    }

    public boolean isInitial() {
        return isInitial;
    }

    public boolean isAccepting() {
        return isAccepting;
    }

    public Map<Character, State> getTransitions() {
        return transitions;
    }

    @Override
    public String toString() {
        return name + (isAccepting ? " (accepting)" : "");
    }
}