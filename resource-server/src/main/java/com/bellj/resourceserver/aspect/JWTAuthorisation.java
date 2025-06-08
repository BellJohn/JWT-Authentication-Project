package com.bellj.resourceserver.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Aspect annotation for ensuring access to a given function is only granted to traffic with
 * specific access roles declared in their JWTs.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JWTAuthorisation {
  // The possible access token role.
  // Either USER or PRIVILEGED
  String value();
}
