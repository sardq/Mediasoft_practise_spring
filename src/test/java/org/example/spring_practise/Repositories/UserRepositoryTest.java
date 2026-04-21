package org.example.spring_practise.Repositories;

import org.example.spring_practise.Entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("john_doe");
        user.setEmail("john@example.com");

        user = entityManager.persistAndFlush(user);
        userId = user.getId();
    }


    @Test
    void existsByUsername_WhenUsernameExists_ShouldReturnTrue() {
         
        boolean exists = userRepository.existsByUsername("john_doe");

         
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_WhenUsernameDoesNotExist_ShouldReturnFalse() {
         
        boolean exists = userRepository.existsByUsername("non_existent_user");

         
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_WithCaseSensitive_ShouldBeCaseSensitive() {
         
        User user2 = new User();
        user2.setUsername("John_Doe");
        user2.setEmail("john2@example.com");
        entityManager.persistAndFlush(user2);

         
        boolean existsLowercase = userRepository.existsByUsername("john_doe");
        boolean existsUppercase = userRepository.existsByUsername("John_Doe");
        boolean existsDifferentCase = userRepository.existsByUsername("JOHN_DOE");

         
        assertThat(existsLowercase).isTrue();
        assertThat(existsUppercase).isTrue();
        assertThat(existsDifferentCase).isFalse();
    }

    @Test
    void existsByUsername_WithEmptyString_ShouldReturnFalse() {
         
        boolean exists = userRepository.existsByUsername("");

         
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_WithNull_ShouldReturnFalse() {
         
        boolean exists = userRepository.existsByUsername(null);

         
        assertThat(exists).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "admin",
            "user123",
            "test_user",
            "john.doe",
            "john-doe"
    })
    void existsByUsername_WithVariousUsernames_ShouldWork(String username) {
         
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(username + "@example.com");
        entityManager.persistAndFlush(newUser);

         
        boolean exists = userRepository.existsByUsername(username);

         
        assertThat(exists).isTrue();
    }

    // ==================== existsByEmail ====================

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
         
        boolean exists = userRepository.existsByEmail("john@example.com");

         
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WhenEmailDoesNotExist_ShouldReturnFalse() {
         
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

         
        assertThat(exists).isFalse();
    }


    @Test
    void existsByEmail_WithEmptyString_ShouldReturnFalse() {
         
        boolean exists = userRepository.existsByEmail("");

         
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_WithNull_ShouldReturnFalse() {
         
        boolean exists = userRepository.existsByEmail(null);

         
        assertThat(exists).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user1@test.com",
            "user.name@domain.com",
            "user+tag@domain.com",
            "user@sub.domain.com",
            "user@domain.co.uk"
    })
    void existsByEmail_WithVariousEmailFormats_ShouldWork(String email) {
         
        User newUser = new User();
        newUser.setUsername("user_" + email.hashCode());
        newUser.setEmail(email);
        entityManager.persistAndFlush(newUser);

         
        boolean exists = userRepository.existsByEmail(email);

         
        assertThat(exists).isTrue();
    }


    @Test
    void save_ShouldPersistUser() {
         
        User newUser = new User();
        newUser.setUsername("new_user");
        newUser.setEmail("new@example.com");

        User savedUser = userRepository.save(newUser);
        entityManager.flush();

         
        assertThat(savedUser.getId()).isNotNull();

        User foundUser = entityManager.find(User.class, savedUser.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("new_user");
        assertThat(foundUser.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void save_WithMinimalFields_ShouldWork() {
         
        User minimalUser = new User();
        minimalUser.setUsername("minimal");
        minimalUser.setEmail("minimal@example.com");

        User savedUser = userRepository.save(minimalUser);
        entityManager.flush();

         
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("minimal");
        assertThat(savedUser.getEmail()).isEqualTo("minimal@example.com");
    }

    @Test
    void findById_ShouldReturnUser() {
         
        Optional<User> found = userRepository.findById(userId);

         
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(userId);
        assertThat(found.get().getUsername()).isEqualTo("john_doe");
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
         
        Optional<User> found = userRepository.findById(UUID.randomUUID());

         
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
         
        User user2 = new User();
        user2.setUsername("jane_doe");
        user2.setEmail("jane@example.com");
        entityManager.persistAndFlush(user2);

         
        List<User> users = userRepository.findAll();

         
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername)
                .containsExactlyInAnyOrder("john_doe", "jane_doe");
    }

    @Test
    void findAll_WithPagination_ShouldReturnPage() {
         
        for (int i = 1; i <= 25; i++) {
            User additionalUser = new User();
            additionalUser.setUsername("user" + i);
            additionalUser.setEmail("user" + i + "@example.com");
            entityManager.persist(additionalUser);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

         
        Page<User> page = userRepository.findAll(pageable);

         
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(26);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    void findAll_WithSorting_ShouldReturnSortedUsers() {
         
        User user2 = new User();
        user2.setUsername("alice");
        user2.setEmail("alice@example.com");
        entityManager.persistAndFlush(user2);

        User user3 = new User();
        user3.setUsername("bob");
        user3.setEmail("bob@example.com");
        entityManager.persistAndFlush(user3);

        Sort sort = Sort.by(Sort.Direction.ASC, "username");

         
        List<User> users = userRepository.findAll(sort);

         
        assertThat(users).hasSize(3);
        assertThat(users.get(0).getUsername()).isEqualTo("alice");
        assertThat(users.get(1).getUsername()).isEqualTo("bob");
        assertThat(users.get(2).getUsername()).isEqualTo("john_doe");
    }

    @Test
    void delete_ShouldRemoveUser() {
         
        userRepository.delete(user);
        entityManager.flush();

        User foundUser = entityManager.find(User.class, userId);
        assertThat(foundUser).isNull();
    }

    @Test
    void deleteById_ShouldRemoveUser() {
         
        userRepository.deleteById(userId);
        entityManager.flush();

        User foundUser = entityManager.find(User.class, userId);
        assertThat(foundUser).isNull();
    }

    @Test
    void existsById_ShouldReturnTrueForExistingUser() {
         
        boolean exists = userRepository.existsById(userId);

        assertThat(exists).isTrue();
    }

    @Test
    void existsById_ShouldReturnFalseForNonExistingUser() {
         
        boolean exists = userRepository.existsById(UUID.randomUUID());

        assertThat(exists).isFalse();
    }

    @Test
    void count_ShouldReturnCorrectNumberOfUsers() {
         
        long initialCount = userRepository.count();

        User newUser = new User();
        newUser.setUsername("new_user");
        newUser.setEmail("new@example.com");
        userRepository.save(newUser);
        entityManager.flush();

        long newCount = userRepository.count();

        assertThat(newCount).isEqualTo(initialCount + 1);
    }


    @Test
    void existsByUsername_WithSpecialCharacters_ShouldWork() {
         
        User specialUser = new User();
        specialUser.setUsername("user_123-test");
        specialUser.setEmail("special@example.com");
        entityManager.persistAndFlush(specialUser);

        boolean exists = userRepository.existsByUsername("user_123-test");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WithSpecialCharacters_ShouldWork() {
         
        User specialUser = new User();
        specialUser.setUsername("special_user");
        specialUser.setEmail("user.name+tag@example.co.uk");
        entityManager.persistAndFlush(specialUser);

        boolean exists = userRepository.existsByEmail("user.name+tag@example.co.uk");

         
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_WithLeadingAndTrailingSpaces_ShouldNotMatch() {
         
        User userWithSpaces = new User();
        userWithSpaces.setUsername("user_without_spaces");
        userWithSpaces.setEmail("nospaces@example.com");
        entityManager.persistAndFlush(userWithSpaces);

        boolean existsWithLeadingSpace = userRepository.existsByUsername(" user_without_spaces");
        boolean existsWithTrailingSpace = userRepository.existsByUsername("user_without_spaces ");
        boolean existsWithBothSpaces = userRepository.existsByUsername(" user_without_spaces ");

        assertThat(existsWithLeadingSpace).isFalse();
        assertThat(existsWithTrailingSpace).isFalse();
        assertThat(existsWithBothSpaces).isFalse();
    }

    @Test
    void save_WithVeryLongFields_ShouldWork() {
         
        String longString = "a".repeat(255);
        User longUser = new User();
        longUser.setUsername(longString);
        longUser.setEmail(longString.substring(0, 200) + "@example.com");

        User savedUser = userRepository.save(longUser);
        entityManager.flush();

        assertThat(savedUser.getId()).isNotNull();

        User foundUser = entityManager.find(User.class, savedUser.getId());
        assertThat(foundUser.getUsername()).isEqualTo(longString);
    }

    @Test
    void deleteAll_ShouldRemoveAllUsers() {
         
        userRepository.deleteAll();
        entityManager.flush();

         
        assertThat(userRepository.count()).isZero();
    }

    @Test
    void findAllById_ShouldReturnMultipleUsers() {
         
        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2 = entityManager.persistAndFlush(user2);

        List<UUID> ids = List.of(userId, user2.getId());

         
        List<User> users = userRepository.findAllById(ids);

         
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getId).containsExactlyInAnyOrder(userId, user2.getId());
    }

    @Test
    void saveAll_ShouldPersistMultipleUsers() {
         
        User user2 = new User();
        user2.setUsername("batch_user1");
        user2.setEmail("batch1@example.com");

        User user3 = new User();
        user3.setUsername("batch_user2");
        user3.setEmail("batch2@example.com");

         
        List<User> savedUsers = userRepository.saveAll(List.of(user2, user3));
        entityManager.flush();

         
        assertThat(savedUsers).hasSize(2);
        assertThat(savedUsers.get(0).getId()).isNotNull();
        assertThat(savedUsers.get(1).getId()).isNotNull();

        assertThat(userRepository.count()).isEqualTo(3);
    }

    @Test
    void existsByUsername_AfterUserDeletion_ShouldReturnFalse() {
         
        String username = user.getUsername();

        userRepository.delete(user);
        entityManager.flush();

        boolean exists = userRepository.existsByUsername(username);
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_AfterUserDeletion_ShouldReturnFalse() {
         
        String email = user.getEmail();

        userRepository.delete(user);
        entityManager.flush();

        boolean exists = userRepository.existsByEmail(email);
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_And_ExistsByEmail_ShouldWorkIndependently() {
         
        User user2 = new User();
        user2.setUsername("unique_username");
        user2.setEmail("unique_email@example.com");
        entityManager.persistAndFlush(user2);

        assertThat(userRepository.existsByUsername("unique_username")).isTrue();
        assertThat(userRepository.existsByUsername("john_doe")).isTrue();
        assertThat(userRepository.existsByEmail("unique_email@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("john@example.com")).isTrue();

        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }
}