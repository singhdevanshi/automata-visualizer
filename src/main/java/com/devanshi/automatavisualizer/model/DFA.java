package com.devanshi.automatavisualizer.model;

import java.util.HashSet;
import java.util.Set;

public class DFA {
    private Set<State> states;
    private Set<Character> alphabet;
    private State initialState;

    public DFA() {
        this.states = new HashSet<>();
        this.alphabet = new HashSet<>();
    }

    public void addState(State state) {
        states.add(state);
        if (state.isInitial()) {
            this.initialState = state;
        }
    }

    public void addSymbol(char symbol) {
        alphabet.add(symbol);
    }

    public boolean accepts(String input) {
        if (initialState == null) {
            throw new IllegalStateException("DFA has no initial state");
        }

        State currentState = initialState;

        for (char symbol : input.toCharArray()) {
            // System.out.println("Current State: " + currentState.getName());
            // System.out.println("Transition on symbol '" + symbol + "': " +
            // currentState.getTransition(symbol));

            currentState = currentState.getTransition(symbol);
            if (currentState == null) {
                System.out.println("No valid transition found for symbol '" + symbol + "'. Input rejected.");
                return false; // No valid transition, reject the input
            }
        }

        System.out.println("Final State: " + currentState.getName());
        System.out.println("Is Final State Accepting: " + currentState.isAccepting());
        return currentState.isAccepting(); // Accept if end state is a final state
    }

    public Set<State> getStates() {
        return states;
    }

    public Set<Character> getAlphabet() {
        return alphabet;
    }

    public State getInitialState() {
        return initialState;
    }
}
