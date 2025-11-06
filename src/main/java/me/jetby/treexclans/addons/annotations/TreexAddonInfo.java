package me.jetby.treexclans.addons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Метаданные аддона TreexClans.
 * <p>Используется для описания информации, зависимостей и порядка загрузки аддона.</p>
 *
 * <pre>{@code
 * @TreexAddonInfo(
 *     id = "clan-shop",
 *     version = "2.2.0",
 *     authors = {"JetBy"},
 *     description = "Клановый магазин.",
 *     depends = {
 *         @Dependency(id = "core-api", version = "^1.4"),
 *         @Dependency(id = "database", version = ">=2.0")
 *     },
 *     softDepends = {
 *         @Dependency(id = "economy")
 *     },
 *     loadBefore = {"promo-system"},
 *     loadAfter = {"core-api"}
 * )
 * public final class ClanShopAddon extends TreexAddon { ... }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TreexAddonInfo {

    /** Уникальный идентификатор аддона. */
    String id();

    /** Версия аддона. */
    String version();

    /** Авторы аддона. */
    String[] authors() default {};

    /** Краткое описание. */
    String description() default "";

    /** Обязательные зависимости. */
    Dependency[] depends() default {};

    /** Необязательные зависимости. */
    Dependency[] softDepends() default {};

    /** Загружается до этих аддонов. */
    String[] loadBefore() default {};

    /** Загружается после этих аддонов. */
    String[] loadAfter() default {};
}
