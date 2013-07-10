package com.meltmedia.service;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import com.meltmedia.util.BakedBeanUtils;
import com.meltmedia.util.NoExceptionCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Map;
import java.util.Set;

/**
 * Service to run validation on a bean and throw a WebApplicationException if validation fails.
 *
 * @author @jacobheun
 *
 */
@Singleton
public class ValidationService {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private ValidatorFactory factory;

  public ValidationService() {
    factory = Validation.buildDefaultValidatorFactory();
  }

  /**
   * Run JSR 303 validation on the given bean.
   *
   * If there is a validation error a javax.ws.rs.WebApplicationException will be thrown with the error messages wrapped
   * inside a json object.
   */
  public Boolean runValidationForJaxWS(final Object bean, final Class<?>... groups) {
    final Validator validator = factory.getValidator();

    validate(bean, new NoExceptionCallable<Set<ConstraintViolation<Object>>>() {
      @Override
      public Set<ConstraintViolation<Object>> call() {
        return validator.validate(bean, groups);
      }
    });

    return true;
  }

  /**
   * Run JSR 303 validation on a single field
   *
   * If there is a validation error a javax.ws.rs.WebApplicationException will be thrown with the error messages wrapped
   * inside a json object.
   */
  public Boolean runValidationForJaxWS(final Object bean, final String fieldName, final Class<?>... groups) {
    final Validator validator = factory.getValidator();

    validate(bean, new NoExceptionCallable<Set<ConstraintViolation<Object>>>() {
      @Override
      public Set<ConstraintViolation<Object>> call() {
        return validator.validateProperty(bean, fieldName, groups);
      }
    });

    return true;
  }

  private void validate(final Object bean, NoExceptionCallable<Set<ConstraintViolation<Object>>> validationDelegate) {
    // Trim all strings
    try {
      BakedBeanUtils.trimStrings(bean);
    } catch (BakedBeanUtils.HalfBakedBeanException ex) {
      log.warn("Error trimming strings on incoming bean", ex);
    }

    Set<ConstraintViolation<Object>> constraintViolations = validationDelegate.call();

    checkOrThrowException(constraintViolations.size() == 0, constraintViolations, Status.BAD_REQUEST);
  }

  private void checkOrThrowException(boolean assertion, Set<ConstraintViolation<Object>> violations, Status status) {
    if( !assertion ) {
      StringBuilder validationMessages = new StringBuilder();
      for (ConstraintViolation<Object> violation : violations) {
        validationMessages.append(violation.getMessage());
      }

      final Map<String, String> entity = ImmutableMap.of("message", validationMessages.toString());

      throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(entity).header("Content-type", MediaType.APPLICATION_JSON).build());
    }
  }
}
