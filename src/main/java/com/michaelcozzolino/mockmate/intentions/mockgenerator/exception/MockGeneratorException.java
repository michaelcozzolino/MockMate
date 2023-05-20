package com.michaelcozzolino.mockmate.intentions.mockgenerator.exception;

import com.michaelcozzolino.mockmate.intentions.mockgenerator.contract.MockGeneratorExceptionInterface;

public class MockGeneratorException extends Exception implements MockGeneratorExceptionInterface {
    public MockGeneratorException(String message) {
        super(message);
    }
}
