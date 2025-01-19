package com.tutopedia.backend.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.tutopedia.backend.services.oci.OciStorageException;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({TutorialNotFoundException.class})
    protected ResponseEntity<Object> handleTutorialNotFound(Exception e, WebRequest request) {
    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tutorial not found");
    }

    @ExceptionHandler({TutorialIdMismatchException.class, FilePersistException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<Object> handleTutorialIdMismatch(Exception e, WebRequest request) {
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tutorial mismatch");
    }

    @ExceptionHandler({RepositoryNotFoundException.class})
    protected ResponseEntity<Object> handleRepositoryNotFound(Exception e, WebRequest request) {
    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Repository not found");
    }

    @ExceptionHandler({RepositoryDuplicateException.class})
    protected ResponseEntity<Object> handleRepositoryDuplicate(Exception e, WebRequest request) {
    	return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate repository");
    }

    @ExceptionHandler({OciStorageException.class})
    protected ResponseEntity<Object> handleOCIException(Exception e, WebRequest request) {
    	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler({TutorialFileNotFoundException.class})
    protected ResponseEntity<Object> handleTutorialFileNotFound(Exception e, WebRequest request) {
    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tutorial File Not Found");
    }

    @ExceptionHandler({InvalidSettingKeyException.class})
    protected ResponseEntity<Object> handleInvalidSettingKey(Exception e, WebRequest request) {
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Key has invalid format");
    }
 }
