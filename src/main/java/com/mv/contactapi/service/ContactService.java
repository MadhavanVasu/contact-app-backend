package com.mv.contactapi.service;

import com.mv.contactapi.entity.Contact;
import com.mv.contactapi.repository.ContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import static com.mv.contactapi.constant.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {

//    @Autowired
//    ContactRepository contactRepository;

    private final ContactRepository contactRepository;

    public Page<Contact> getAllContacts(int page, int size) {
        return contactRepository.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    public Contact getContactById(String id) {
        // Optional.orElseThrow() -> Accepts a Supplier that supplies an object of type Throwable.
        return contactRepository.findByContactId(id).orElseThrow(() -> new RuntimeException("Contact not found"));
    }

    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }

    public void deleteContact(Contact contact) {
        contactRepository.delete(contact);
    }

    // What is Multipart Request?
    // An HTTP multipart request is an HTTP request that HTTP clients construct to send files and data
    // over to an HTTP Server.
    // It is commonly used by browsers and HTTP clients to upload files to the server.

    // What is Multipart file?
    // A representation of an uploaded file received in a multipart request.
    public String uploadPhoto(String id, MultipartFile file) {
        Contact contact = getContactById(id);
        contact.setPhotoURL(photoFunction.apply(id, file));
        contactRepository.save(contact);
        log.info("Photo uploaded successfully - From Contact Service");
        return contact.getPhotoURL();
    }

    private final UnaryOperator<String> getFileExtension = filename -> Optional.of(filename)
            .filter(name -> name.contains("."))
            .map(name -> name.substring(name.lastIndexOf(".")))
            .orElse(".png");

    private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) ->
    {
        String filename = id + getFileExtension.apply(image.getOriginalFilename());
        try {
            // toAbsolutePath() -> Converts the path to Absolute if the given path is relative
            // normalize() -> Removes redundant elements from path (For example, ".." is preceded by a folder name
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if (!Files.exists(fileStorageLocation)) {
                // Files.createDirectories() -> Creates a directory by creating all nonexistent parent directories first.
                // Unlike the createDirectory method, an exception is not thrown if the directory could not be created
                // because it already exists.
                Files.createDirectories(fileStorageLocation);
            }
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(filename), REPLACE_EXISTING);
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/contacts/image/" + filename)
                    .toUriString();

        } catch (Exception exception) {
            throw new RuntimeException("Unable to save image : " + exception);
        }
    };


}
