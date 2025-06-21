/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error.factory;

import java.util.Locale;

import br.arch.sticker.core.error.code.StickerPackErrorCode;
import br.arch.sticker.core.error.throwable.sticker.PackValidatorException;

public class StickerPackExceptionFactory {
    private StickerPackExceptionFactory() {
    }

    public static PackValidatorException emptyStickerPackIdentifier() {
        return new PackValidatorException(
                "O identificador do pacote de figurinhas está vazio!",
                StickerPackErrorCode.INVALID_IDENTIFIER);
    }

    public static PackValidatorException invalidSizeStickerPackIdentifier(int charMax) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O identificador do pacote de figurinhas não pode exceder %d caracteres",
                        charMax),
                StickerPackErrorCode.INVALID_IDENTIFIER);
    }

    public static PackValidatorException emptyStickerPackPublisher(String stickerPackIdentifier) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O publisher do pacote de figurinhas está vazio, identificador do pacote de figurinhas: %s",
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_PUBLISHER);
    }

    public static PackValidatorException invalidSizePublisherStickerPack(int charMax, String stickerPackIdentifier) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O publisher do pacote de figurinhas não pode exceder %d caracteres, identificador do pacote de figurinhas: %s",
                        charMax,
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_PUBLISHER);
    }

    public static PackValidatorException emptyStickerPackName(String stickerPackIdentifier) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Nome do pacote de figurinhas está vazio, identificador do pacote de figurinhas: %s",
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_STICKERPACK_NAME);
    }

    public static PackValidatorException invalidSizeNameStickerPack(int charMax, String stickerPackIdentifier) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O nome do pacote de figurinhas não pode exceder %d caracteres, identificador do pacote de figurinhas: %s",
                        charMax,
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_PUBLISHER);
    }

    public static PackValidatorException emptyTrayImage(String stickerPackIdentifier) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "A thumbnail do pacote de figurinhas está vazia, identificador do pacote de figurinhas: %s",
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_THUMBNAIL);
    }

    public static PackValidatorException invalidAndroidPlayStoreUrl(String url) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Certifique-se de incluir http ou https nas URL, o link da Android Play Store não é uma URL válida: %s",
                        url),
                StickerPackErrorCode.INVALID_ANDROID_URL_SITE);
    }

    public static PackValidatorException invalidAndroidPlayStoreDomain(String domain) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O link da Android Play Store deve usar o domínio da Play Store: %s",
                        domain),
                StickerPackErrorCode.INVALID_ANDROID_URL_SITE);
    }

    public static PackValidatorException invalidIosAppStoreUrl(String url) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Certifique-se de incluir http ou https nos links de URL, o link da loja de aplicativos iOS não é uma URL válida: %s",
                        url),
                StickerPackErrorCode.INVALID_IOS_URL_SITE);
    }

    public static PackValidatorException invalidIosAppStoreDomain(String domain) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O link da loja de aplicativos iOS deve usar o domínio da loja de aplicativos: %s",
                        domain),
                StickerPackErrorCode.INVALID_IOS_URL_SITE);
    }

    public static PackValidatorException invalidLicenseAgreementUrl(String url) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Certifique-se de incluir http ou https nos links de URL, o link do contrato de licença não é uma URL válida: %s",
                        url),
                StickerPackErrorCode.INVALID_WEBSITE);
    }

    public static PackValidatorException invalidPrivacyPolicyUrl(String url) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Certifique-se de incluir http ou https nos links de URL, o link da política de privacidade não é uma URL válida: %s",
                        url),
                StickerPackErrorCode.INVALID_WEBSITE);
    }

    public static PackValidatorException invalidPublisherWebsite(String url) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Certifique-se de incluir http ou https nos links de URL, o link do site do editor não é uma URL válida: %s",
                        url),
                StickerPackErrorCode.INVALID_WEBSITE);
    }

    public static PackValidatorException invalidPublisherEmail(String email) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O e-mail do publisher não parece válido, o e-mail é: %s",
                        email),
                StickerPackErrorCode.INVALID_EMAIL);
    }

    public static PackValidatorException trayImageTooLarge(String fileName, int maxKb) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "A imagem da thumbnail deve ter menos de %d KB, arquivo de thumbnail: %s",
                        maxKb,
                        fileName),
                StickerPackErrorCode.INVALID_THUMBNAIL);
    }

    public static PackValidatorException invalidTrayImageHeight(String fileName, int height, int min, int max) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "A altura da thumbnail deve estar entre %d e %d pixels, a altura atual da imagem da bandeja é %d, arquivo: %s",
                        min,
                        max,
                        height,
                        fileName),
                StickerPackErrorCode.INVALID_THUMBNAIL);
    }

    public static PackValidatorException invalidTrayImageWidth(String fileName, int width, int min, int max) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "A largura da thumbnail deve estar entre %d e %d pixels, a largura atual da imagem da bandeja é %d, arquivo: %s",
                        min,
                        max,
                        width,
                        fileName),
                StickerPackErrorCode.INVALID_THUMBNAIL);
    }

    public static PackValidatorException cannotOpenTrayImage(String fileName, Throwable cause) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Não é possível abrir a thumbnail: %s",
                        fileName),
                cause,
                StickerPackErrorCode.INVALID_THUMBNAIL);
    }

    public static PackValidatorException invalidStickerCount(int count, String stickerPackIdentifier) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "A quantidade de figurinhas do pacote deve estar entre 3 a 30, atualmente tem %d, identificador do pacote de figurinhas: %s",
                        count,
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_STICKERPACK_SIZE);
    }

    public static PackValidatorException invalidStickerPackString(String offendingString) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "%s contém caracteres inválidos, os caracteres permitidos são de a a z, A a Z, _, ' - . e caractere de espaço",
                        offendingString),
                StickerPackErrorCode.INVALID_STICKERPACK_NAME);
    }

    public static PackValidatorException stickerPackStringContainsDotDot(String offendingString) {
        return new PackValidatorException(
                String.format(
                        Locale.ROOT,
                        "%s não pode conter ..",
                        offendingString),
                StickerPackErrorCode.INVALID_STICKERPACK_NAME);
    }
}