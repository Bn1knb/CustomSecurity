package com.epam.kamisarau.auth.repository.hibernate;

import com.epam.kamisarau.auth.exception.NoUserFound;
import com.epam.kamisarau.auth.exception.RegistrationFailedException;
import com.epam.kamisarau.auth.model.UserModel;
import com.epam.kamisarau.auth.repository.UserRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class HbUserRepositoryImpl implements UserRepository {
    private SessionFactory sessionFactory;
    private Transaction transaction;

    @Autowired
    public HbUserRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public UserModel getUser(Long userId) {
        Optional<UserModel> user = Optional.empty();
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            user = Optional.of(
                    session.load(UserModel.class, userId)
            );

            transaction.commit();
        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return user.orElseThrow(NoUserFound::new);
    }

    @Override
    public UserModel getUserByUsername(String username) throws NoUserFound {
        Optional<UserModel> user = Optional.empty();

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            user = Optional.of(
                    (UserModel) session.createQuery("", UserModel.class)
            );

            transaction.commit();
        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return user.orElseThrow(NoUserFound::new);
    }

    @Override
    public UserModel doRegister(UserModel user) throws RegistrationFailedException {
        UserModel registeredUser;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            e.printStackTrace();
        }

        try {
            registeredUser = getUserByUsername(user.getUsername());
        } catch (NoUserFound e) {
            throw new RegistrationFailedException();
        }

        return registeredUser;
    }
}