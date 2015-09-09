package com.lhkbob.fxsl.parser.semantics;

import com.lhkbob.fxsl.lang.Environment;

/**
 *
 */
public interface SemanticsChecker {
  boolean continueOnFailure();

  // FIXME API design choice; given that this whole interface is to check for semantic issues does it
  // make sense that this effective return value (the set of problems) is exposed as an exception and
  // not as a return value? Is the stack trace and optional message useful?
  void validate(Environment environment) throws SemanticsException;
}
