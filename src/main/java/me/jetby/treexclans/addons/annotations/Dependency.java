package me.jetby.treexclans.addons.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Описание зависимости аддона.
 * <p>Поддерживаются версии:</p>
 * <ul>
 *   <li>{@code >=1.0.0}, {@code >1.2}, {@code ==2.0}, {@code <=3.0}</li>
 *   <li>{@code ^1.4} — совместимость по мажорной версии</li>
 *   <li>{@code 1.x} — любая минорная версия</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Dependency {

    /** Идентификатор зависимого аддона. */
    String id();

    /** Требуемая версия (по умолчанию — любая). */
    String version() default "";
}
