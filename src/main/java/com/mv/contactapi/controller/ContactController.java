package com.mv.contactapi.controller;

import com.mv.contactapi.entity.Contact;
import com.mv.contactapi.service.ContactService;
import com.mv.contactapi.service.MyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.mv.contactapi.constant.Constant.PHOTO_DIRECTORY;

@RestController
@RequestMapping("contacts")
@Slf4j
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    private final MyService myService;

    @PostMapping
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        // The ResponseEntity.created() method is used to create a response entity with an
        // HTTP status code of 201 Created.
        // This status code indicates that the request has been successfully fulfilled and
        // resulted in the creation of a new resource.
        //It requires an argument of type URI, which represents the location of the newly created resource.
        // This URI is typically provided in the Location header of the HTTP response.
        return ResponseEntity.created(URI.create("/contacts/id")).body(contactService.createContact(contact));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok().body(contactService.getContactById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Contact>> getAllContacts(@RequestParam(value = "page") int page, @RequestParam(value = "size") int size) {
//        return ResponseEntity.ok().body(contactService.getAllContacts(page, size).stream().toList());
        return ResponseEntity.ok().body(contactService.getAllContacts(page, size));
    }

    @PutMapping("/uploadPhoto")
    public ResponseEntity<String> uploadPhoto(@RequestParam(value = "id") String id, @RequestParam(value = "file") MultipartFile file) {
//        return ResponseEntity.ok().body(contactService.uploadPhoto(id, file));
        return ResponseEntity.ok().body(myService.uploadPhoto(id, file));
    }

    private final UnaryOperator<String> getFilename = fileUrl -> {
        try {
            return Optional.of(fileUrl)
                    .filter(name -> name.contains("."))
                    .map(name -> name.substring(0, name.lastIndexOf(".")))
                    .orElseThrow(() -> new Exception("Invalid filename or file not found"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

    // produces -> produces takes in an array.
    // So we can mention what all media types we can return from this controller.
    @GetMapping(value = "/image/{filename}", produces = {MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> getPhoto(@PathVariable(value = "filename") String filename) throws IOException {
//        return ResponseEntity.ok().body(Files.readAllBytes(Paths.get(PHOTO_DIRECTORY + filename)));
        return ResponseEntity.ok().body(myService.downloadPhoto(filename));
    }

    @DeleteMapping(value = "/image/{filename}")
    public ResponseEntity<String> deletePhoto(@PathVariable(value = "filename") String filename) {
//        return ResponseEntity.ok().body(Files.readAllBytes(Paths.get(PHOTO_DIRECTORY + filename)));
        return ResponseEntity.ok().body(myService.deletePhoto(filename));
    }
}
