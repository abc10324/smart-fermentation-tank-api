package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.model.dao.AlertDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class AlertService {

    @Autowired
    private AlertDao alertDao;
}
