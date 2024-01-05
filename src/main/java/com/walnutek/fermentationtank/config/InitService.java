package com.walnutek.fermentationtank.config;

import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.service.CipherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InitService {

    @Autowired
    private CipherService cipherService;

    @Autowired
    private MongoTemplate template;

    private static final List<String> COLLECTION_LIST = List.of(
            Const.COLLECTION_USER,
            Const.COLLECTION_LABORATORY
    );

    public void init() {
        dropAllCollection();
        createAllCollection();
        createAdminUser();
    }

    private void dropAllCollection() {
        for(var collection : template.getCollectionNames()) {
            template.dropCollection(collection);
        }
    }

    private void createAllCollection() {
        for(var collection : COLLECTION_LIST) {
            template.createCollection(collection);
        }
    }

    private void createAdminUser() {
        var data = new User();
        data.setRole(User.Role.SUPER_ADMIN);
        data.setAccount("admin");
        data.setPassword(cipherService.encrypt("admin"));
        data.setName("管理員");

        template.save(data);
    }


}
