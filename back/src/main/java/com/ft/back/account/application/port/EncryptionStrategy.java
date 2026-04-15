package com.ft.back.account.application.port;

public interface EncryptionStrategy {
    String encrypt(String plainText);
    String decrypt(String cipherText);
}
