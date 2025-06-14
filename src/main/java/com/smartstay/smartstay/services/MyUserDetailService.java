package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.UserPrinciple;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    UserRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersRepository.findByEmailId(username);

        if (user == null) {
            return null;
        }
        return new UserPrinciple(user);
    }
}
