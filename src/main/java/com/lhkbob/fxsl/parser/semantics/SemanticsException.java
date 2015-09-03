package com.lhkbob.fxsl.parser.semantics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class SemanticsException extends Exception {
  private final List<SemanticsProblem> problems;

  public SemanticsException(SemanticsProblem... problems) {
    this(Arrays.asList(problems));
  }

  public SemanticsException(Collection<? extends SemanticsProblem> problems) {
    this("", problems);
  }

  public SemanticsException(String message, Collection<? extends SemanticsProblem> problems) {
    this(message, null, problems);
  }

  public SemanticsException(
      String message, Throwable cause, Collection<? extends SemanticsProblem> problems) {
    super(formMessage(message, problems), cause);
    this.problems = Collections.unmodifiableList(new ArrayList<>(problems));
  }

  private static String formMessage(
      String overallMessage, Collection<? extends SemanticsProblem> problems) {
    // If no problems are given, return the overall message regardless of what it is (even if it's blank).
    // This case is clearly when the thrower has figured out the explicit message to give.
    if (problems.isEmpty()) {
      return overallMessage;
    }

    StringBuilder sb = new StringBuilder();
    if (overallMessage != null && !overallMessage.isEmpty()) {
      // Use overallMessage as the banner
      sb.append(overallMessage).append(":\n\n");
    } else {
      // Use a standard banner
      sb.append("Semantic failures:\n\n");
    }

    for (SemanticsProblem p : problems) {
      sb.append(p).append("\n");
    }

    return sb.toString();
  }

  public SemanticsException(Throwable cause, SemanticsProblem... problems) {
    this(cause, Arrays.asList(problems));
  }

  public SemanticsException(Throwable cause, Collection<? extends SemanticsProblem> problems) {
    this("", cause, problems);
  }

  public SemanticsException(String message, SemanticsProblem... problems) {
    this(message, Arrays.asList(problems));
  }

  public SemanticsException(String message, Throwable cause, SemanticsProblem... problems) {
    this(message, cause, Arrays.asList(problems));
  }

  public static List<SemanticsProblem> combineProblems(
      List<SemanticsProblem> previous, List<SemanticsProblem> newest) {
    if (newest != null) {
      if (previous != null) {
        // add to the previous one
        previous.addAll(newest);
        return previous;
      } else {
        // convert newest into an appendable list
        return new ArrayList<>(newest);
      }
    } else {
      return previous;
    }
  }

  public List<SemanticsProblem> getProblems() {
    return problems;
  }
}
