package com.github.nhirakawa.wilson.models.style;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Target({ ElementType.PACKAGE, ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
@JsonSerialize
@Value.Style(
  get = { "is*", "get*" }, // Detect 'get' and 'is' prefixes in accessor methods
  init = "set*", // Builder initialization methods will have 'set' prefix
  typeAbstract = { "Abstract*", "*IF", "*Model" }, // 'Abstract' prefix will be detected and trimmed
  typeImmutable = "*", // No prefix or suffix for generated immutable type
  visibility = ImplementationVisibility.SAME
//    defaults = @Value.Immutable(copy = false)
)
public @interface WilsonStyle {
}
