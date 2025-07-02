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
import br.arch.sticker.core.error.throwable.sticker.StickerPackValidatorException;

public class StickerPackExceptionFactory {
    private StickerPackExceptionFactory() {
    }

    public static StickerPackValidatorException emptyStickerPackIdentifier() {
        return new StickerPackValidatorException(
                "O identificador do pacote de figurinhas está vazio!",
                StickerPackErrorCode.INVALID_IDENTIFIER);
    }

    public static StickerPackValidatorException invalidSizeStickerPackIdentifier(int charMax) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O identificador do pacote de figurinhas não pode exceder %d caracteres",
                        charMax),
                StickerPackErrorCode.INVALID_IDENTIFIER);
    }

    public static StickerPackValidatorException emptyStickerPackPublisher(String stickerPackIdentifier) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O publisher do pacote de figurinhas está vazio, identificador do pacote de figurinhas: %s",
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_PUBLISHER);
    }

    public static StickerPackValidatorException invalidSizePublisherStickerPack(int charMax, String stickerPackIdentifier) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O publisher do pacote de figurinhas não pode exceder %d caracteres, identificador do pacote de figurinhas: %s",
                        charMax,
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_PUBLISHER);
    }

    public static StickerPackValidatorException emptyStickerPackName(String stickerPackIdentifier) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Nome do pacote de figurinhas está vazio, identificador do pacote de figurinhas: %s",
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_STICKERPACK_NAME);
    }

    public static StickerPackValidatorException invalidSizeNameStickerPack(int charMax, String stickerPackIdentifier) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O nome do pacote de figurinhas não pode exceder %d caracteres, identificador do pacote de figurinhas: %s",
                        charMax,
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_PUBLISHER);
    }

    public static StickerPackValidatorException emptyTrayImage(String stickerPackIdentifier) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "A thumbnail do pacote de figurinhas está vazia, identificador do pacote de figurinhas: %s",
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_THUMBNAIL);
    }

    public static StickerPackValidatorException invalidAndroidPlayStoreUrl(String url) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Certifique-se de incluir http ou https nas URL, o link da Android Play Store não é uma URL válida: %s",
                        url),
                StickerPackErrorCode.INVALID_ANDROID_URL_SITE);
    }

    public static StickerPackValidatorException invalidAndroidPlayStoreDomain(String domain) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O link da Android Play Store deve usar o domínio da Play Store: %s",
                        domain),
                StickerPackErrorCode.INVALID_ANDROID_URL_SITE);
    }

    public static StickerPackValidatorException invalidIosAppStoreUrl(String url) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Certifique-se de incluir http ou https nos links de URL, o link da loja de aplicativos iOS não é uma URL válida: %s",
                        url),
                StickerPackErrorCode.INVALID_IOS_URL_SITE);
    }

    public static StickerPackValidatorException invalidIosAppStoreDomain(String domain) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O link da loja de aplicativos iOS deve usar o domínio da loja de aplicativos: %s",
                        domain),
                StickerPackErrorCode.INVALID_IOS_URL_SITE);
    }

    public static StickerPackValidatorException invalidLicenseAgreementUrl(String url) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Certifique-se de incluir http ou https nos links de URL, o link do contrato de licença não é uma URL válida: %s",
                        url),
                StickerPackErrorCode.INVALID_WEBSITE);
    }

    public static StickerPackValidatorException invalidPrivacyPolicyUrl(String url) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Certifique-se de incluir http ou https nos links de URL, o link da política de privacidade não é uma URL válida: %s",
                        url),
                StickerPackErrorCode.INVALID_WEBSITE);
    }

    public static StickerPackValidatorException invalidPublisherWebsite(String url) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Certifique-se de incluir http ou https nos links de URL, o link do site do editor não é uma URL válida: %s",
                        url),
                StickerPackErrorCode.INVALID_WEBSITE);
    }

    public static StickerPackValidatorException invalidPublisherEmail(String email) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "O e-mail do publisher não parece válido, o e-mail é: %s",
                        email),
                StickerPackErrorCode.INVALID_EMAIL);
    }

    public static StickerPackValidatorException trayImageTooLarge(String fileName, int maxKb) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "A imagem da thumbnail deve ter menos de %d KB, arquivo de thumbnail: %s",
                        maxKb,
                        fileName),
                StickerPackErrorCode.INVALID_THUMBNAIL);
    }

    public static StickerPackValidatorException invalidTrayImageHeight(String fileName, int height, int min, int max) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "A altura da thumbnail deve estar entre %d e %d pixels, a altura atual da imagem da bandeja é %d, arquivo: %s",
                        min,
                        max,
                        height,
                        fileName),
                StickerPackErrorCode.INVALID_THUMBNAIL);
    }

    public static StickerPackValidatorException invalidTrayImageWidth(String fileName, int width, int min, int max) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "A largura da thumbnail deve estar entre %d e %d pixels, a largura atual da imagem da bandeja é %d, arquivo: %s",
                        min,
                        max,
                        width,
                        fileName),
                StickerPackErrorCode.INVALID_THUMBNAIL);
    }

    public static StickerPackValidatorException cannotOpenTrayImage(String fileName, Throwable cause) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "Não é possível abrir a thumbnail: %s",
                        fileName),
                cause,
                StickerPackErrorCode.INVALID_THUMBNAIL);
    }

    public static StickerPackValidatorException invalidStickerCount(int count, String stickerPackIdentifier) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "A quantidade de figurinhas do pacote deve estar entre 3 a 30, atualmente tem %d, identificador do pacote de figurinhas: %s",
                        count,
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_STICKERPACK_SIZE);
    }

    public static StickerPackValidatorException invalidStickerPackString(String offendingString) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "%s contém caracteres inválidos, os caracteres permitidos são de a a z, A a Z, _, ' - . e caractere de espaço",
                        offendingString),
                StickerPackErrorCode.INVALID_STICKERPACK_NAME);
    }

    public static StickerPackValidatorException stickerPackStringContainsDotDot(String offendingString) {
        return new StickerPackValidatorException(
                String.format(
                        Locale.ROOT,
                        "%s não pode conter ..",
                        offendingString),
                StickerPackErrorCode.INVALID_STICKERPACK_NAME);
    }
}