package com.devanshi.automatavisualizer.util;

import java.util.Set;

import com.devanshi.automatavisualizer.model.DFA;
import com.devanshi.automatavisualizer.model.State;

public class LanguageParser {

    /**
     * Creates a simple DFA that accepts strings ending with the given suffix.
     */
    public static DFA createEndsWith(String suffix) {
        DFA dfa = new DFA();

        // Add all symbols to alphabet
        for (char c : suffix.toCharArray()) {
            dfa.addSymbol(c);
        }

        // Create states
        State[] states = new State[suffix.length() + 1];
        for (int i = 0; i <= suffix.length(); i++) {
            boolean isAccepting = (i == suffix.length());
            boolean isInitial = (i == 0);
            states[i] = new State("q" + i, isInitial, isAccepting);
            dfa.addState(states[i]);
        }

        // Add transitions for the best case
        for (int i = 0; i < suffix.length(); i++) {
            char symbol = suffix.charAt(i);
            states[i].addTransition(symbol, states[i + 1]);
        }

        // Print missing transitions for each state and create transitions based on
        // conditions
        for (int i = 0; i <= suffix.length(); i++) {
            Set<Character> transitions = states[i].getTransitions().keySet();
            for (char symbol : dfa.getAlphabet()) {
                if (!transitions.contains(symbol)) {
                    // System.out.println(
                    // "State " + states[i].getName() + " is missing transition on symbol '" +
                    // symbol + "'");
                    if (symbol == suffix.charAt(0)) {
                        states[i].addTransition(symbol, states[1]); // Transition to q1 if missing symbol is the first
                                                                    // character of the pattern
                    } else {
                        states[i].addTransition(symbol, states[0]); // Transition to q0 otherwise
                    }
                }
            }
        }

        return dfa;
    }

    /**
     * Creates a simple DFA that accepts strings containing the given substring.
     */
    public static DFA createContains(String substring) {
        DFA dfa = new DFA();

        // Add all symbols to alphabet
        for (char c : substring.toCharArray()) {
            dfa.addSymbol(c);
        }

        // Create states
        State[] states = new State[substring.length() + 1];
        for (int i = 0; i <= substring.length(); i++) {
            boolean isAccepting = (i == substring.length());
            boolean isInitial = (i == 0);
            states[i] = new State("q" + i, isInitial, isAccepting);
            dfa.addState(states[i]);
        }

        // Add transitions
        for (int i = 0; i < substring.length(); i++) {
            for (char s : dfa.getAlphabet()) {
                if (substring.charAt(i) == s && i + 1 <= substring.length()) {
                    states[i].addTransition(s, states[i + 1]); // Ensure valid index access
                } else {
                    // Compute fallback state
                    int backTo = 0;
                    for (int k = 1; k < i; k++) {
                        if (substring.substring(0, k).equals(substring.substring(i - k, i))) {
                            backTo = k;
                        }
                    }

                    if (backTo < substring.length() && substring.charAt(backTo) == s) {
                        states[i].addTransition(s, states[backTo + 1]); // Ensure no out-of-bounds access
                    } else {
                        states[i].addTransition(s, states[0]);
                    }
                }
            }
        }

        // From accepting state, stay there for all inputs
        for (char symbol : dfa.getAlphabet()) {
            states[substring.length()].addTransition(symbol, states[substring.length()]);
        }

        return dfa;
    }
}
