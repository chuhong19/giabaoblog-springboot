package vn.giabaoblog.giabaoblogserver.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.giabaoblog.giabaoblogserver.config.exception.*;
import vn.giabaoblog.giabaoblogserver.data.domains.*;
import vn.giabaoblog.giabaoblogserver.data.dto.request.SearchUserRequest;
import vn.giabaoblog.giabaoblogserver.data.dto.shortName.CreateOrUpdateUserDTO;
import vn.giabaoblog.giabaoblogserver.data.dto.shortName.UserDTO;
import vn.giabaoblog.giabaoblogserver.data.repository.PermissionRepository;
import vn.giabaoblog.giabaoblogserver.data.repository.RoleRepository;
import vn.giabaoblog.giabaoblogserver.data.repository.UserFollowRepository;
import vn.giabaoblog.giabaoblogserver.data.repository.UserRepository;
import vn.giabaoblog.giabaoblogserver.services.support.EmailService;
import vn.giabaoblog.giabaoblogserver.services.validation.PasswordValidatorService;

import javax.management.relation.RoleNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@CommonsLog
@Service
public class UserService {

    @Autowired
    private UserFollowRepository userFollowRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RoleService roleService;

    private PasswordValidatorService passwordValidatorService;

    public UserService() {
        passwordValidatorService = new PasswordValidatorService();
    }

    private String salt = "jS9nJFFbhuIYnkpLg47PQZHD4ylhtBOP";

    public Iterable<User> getUserList() {
        return userRepository.findAll();
    }

    public List<UserDTO> getUserPresentationList() {
        ArrayList<UserDTO> listDto = new ArrayList<>();
        Iterable<User> list = getUserList();
        list.forEach(user -> listDto.add(new UserDTO(user)));
        return listDto;
    }

    public User loadUserById(final Long id) throws NotFoundException {
        Optional<User> opUser = userRepository.findById(id);
        final User user = opUser.orElseThrow(() -> new NotFoundException("ID not found"));
        return user;
    }

    public User loadUserByUsernameOrEmail(String username) throws NotFoundException {
        final User user = userRepository.findByUsernameOrEmail(username).orElseThrow(() -> new NotFoundException("Username not found"));
        return user;
    }

    public boolean emailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            return true;
        }
        return false;
    }

    public boolean userExists(String username) {
        if (userRepository.existsByUsername(username)) {
            return true;
        }
        return false;
    }

    public User getUserByUsername(String username) {
        if (username == null) {
            throw new InvalidUsernameException("Username cannot be null");
        }
        return userRepository.findByUsername(username);
    }

    public User getUserByEmail(String email) {
        if (email == null) {
            throw new InvalidEmailException("email cannot be null");
        }
        return userRepository.findByEmail(email);
    }

    @Transactional
    public UserDTO updateUser(CreateOrUpdateUserDTO updateUserDTO) {
        Long id = updateUserDTO.getUserId();
        if (id == null) {
            throw new InvalidUserDataException("Id cannot be null");
        }
        if (updateUserDTO == null) {
            throw new InvalidUserDataException("User account data cannot be null");
        }
        passwordValidatorService.checkPassword(updateUserDTO.getPassword());
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            throw new UserNotFoundException(String.format("The user with Id = %s doesn't exists", id));
        }
        User user = userOpt.get();

        User userByUsername = getUserByUsername(updateUserDTO.getUsername());
        if (userByUsername != null) {
            if (!user.getId().equals(userByUsername.getId())) {
                String msg = String.format("The username %s it's already in use from another user with ID = %s",
                        updateUserDTO.getUsername(), userByUsername.getId());
                log.error(msg);
                throw new InvalidUserDataException(msg);
            }
        }
        User userEmail = getUserByEmail(updateUserDTO.getEmail());
        if (userEmail != null) {
            if (!user.getId().equals(userEmail.getId())) {
                String msg = String.format("The email %s it's already in use from another user with ID = %s",
                        updateUserDTO.getEmail(), userEmail.getId());
                log.error(msg);
                throw new InvalidUserDataException(msg);
            }
        }
        user.setUsername(updateUserDTO.getUsername());
        user.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        user.setFirstname(updateUserDTO.getFirstname());
        user.setLastname(updateUserDTO.getLastname());
        user.setGender(updateUserDTO.getGender());
        user.setEmail(updateUserDTO.getEmail());
        user.setEnabled(updateUserDTO.isEnabled());

        User userUpdated = userRepository.save(user);
        log.info(String.format("User %s has been updated.", user.getId()));
        UserDTO userDTO = new UserDTO(user);
        return userDTO;
    }

    @Transactional
    public User addRole(Long roleId, Long userId) throws RoleNotFoundException {
        // check user
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new UserNotFoundException(String.format("User not found with Id = %s", userId));
        }
        User user = userOpt.get();

        // check role
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            throw new RoleNotFoundException(String.format("Role not found with Id = %s", roleId));
        }

        Role role = roleOpt.get();

        user.getRoles().add(role);

        userRepository.save(user);
        log.info(String.format("Added role %s on user id = %s", role.getRole(), user.getId()));

        return user;
    }

    @Transactional
    public User removeRole(Long roleId, Long userId) throws RoleNotFoundException {
        // check user
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new UserNotFoundException(String.format("User not found with Id = %s", userId));
        }
        User user = userOpt.get();

        // check role
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            throw new RoleNotFoundException(String.format("Role not found with Id = %s", roleId));
        }

        Role role = roleOpt.get();

        user.getRoles().remove(role);

        userRepository.save(user);
        log.info(String.format("Removed role %s on user id = %s", role.getRole(), user.getId()));

        return user;
    }

    public Set<String> getRoleByUserId(Long userId) {
        // check user
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new UserNotFoundException(String.format("User not found with Id = %s", userId));
        }

        Set<String> roles = new HashSet<>();
        User user = userOpt.get();
        for (Role role : user.getRoles()) {
            roles.add(role.getRole());
        }
        return roles;
    }

    public Set<String> getPermissionByUserId(Long userId) {
        // check user
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new UserNotFoundException(String.format("User not found with Id = %s", userId));
        }

        Set<String> permissions = new HashSet<>();
        User user = userOpt.get();
        // ...
        for (Role role : user.getRoles()) {
            // Lấy danh sách quyền từ mỗi Role và thêm vào danh sách permissions
            permissions.addAll(role.getPermissions().stream()
                    .map(Permission::getPermission)
                    .collect(Collectors.toSet()));
        }
        return permissions;
    }

    public Set<String> getPermissionsByAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();
            return getPermissionByUserId(userId);
        } else {
            // Xử lý khi không thể lấy thông tin User từ SecurityContextHolder
            return Collections.emptySet();
        }
    }

    public Set<String> getPermissionByRoleId(Long roleId) throws RoleNotFoundException {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            throw new RoleNotFoundException(String.format("Role not found with Id = %s", roleId));
        }
        Role role = roleOpt.get();
        Set<String> permissions = new HashSet<>();
        for (Permission permission : role.getPermissions()) {
            permissions.add(permission.getPermission());
        }
        return permissions;
    }

    public boolean checkPermissionByAccessToken(String permissionToCheck) {
        Set<String> acceptedPermissions = getPermissionsByAccessToken();
        return acceptedPermissions.contains(permissionToCheck);
    }

    public void forgotPassword(String username, String email) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(username);
        if (!userOpt.isPresent()) {
            throw new InvalidUsernameException(String.format("User not found with username = %s", username));
        }
        User user = userOpt.get();
        if (!user.getEmail().equals(email)) {
            throw new InvalidUserDataException(String.format("Email not found with email = %s", email));
        }
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        userRepository.save(user);

        emailService.sendResetPasswordEmail(user.getEmail(), resetToken);
    }

    public boolean verifyResetToken(String username, String resetToken) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(username);
        if (!userOpt.isPresent()) {
            throw new InvalidUsernameException(String.format("User not found with username = %s", username));
        }
        User user = userOpt.get();
        if (!user.getResetToken().equals(resetToken)) {
            throw new InvalidUserDataException(String.format("Reset token not match with reset token = %s", resetToken));
        }
        return true;
    }

    public void resetPassword(String resetToken, String newPassword) {
        User user = userRepository.findByResetToken(resetToken)
                .orElseThrow(() -> new InvalidUserDataException("Invalid reset token"));
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new InvalidUserDataException("Please don't use your old password!");
        }
        passwordValidatorService.checkPassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }

    public void changePassword(Long userId, String password, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new InvalidUsernameException(String.format("User not found with id = %s", userId));
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidUserDataException("Wrong password");
        }
        passwordValidatorService.checkPassword(newPassword);
        if (newPassword.equals(password)) {
            throw new InvalidUserDataException("Please don't use your old password!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new InvalidUsernameException(String.format("User not found with id = %s", userId));
        }
        User user = userOpt.get();
        userRepository.delete(user);
    }

    public void disableUser(Long userId) {
        System.out.println("...");
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new InvalidUsernameException(String.format("User not found with id = %s", userId));
        }
        User user = userOpt.get();
        user.setEnabled(false);
        userRepository.save(user);
    }

    public void enableUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new InvalidUsernameException(String.format("User not found with id = %s", userId));
        }
        User user = userOpt.get();
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void addSpecialPermission(Long permissionId, Long userId) {
        // check user
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new UserNotFoundException(String.format("User not found with Id = %s", userId));
        }
        User user = userOpt.get();

        // check permission
        Optional<Permission> permissionOpt = permissionRepository.findById(permissionId);
        if (!permissionOpt.isPresent()) {
            throw new PermissionNotFoundException(String.format("Permission not found with Id = %s", permissionId));
        }

        Permission permission = permissionOpt.get();

        user.getSpecial_permissions().add(permission);

        userRepository.save(user);
        log.info(String.format("Added special permission %s on user id = %s", permission.getPermission(), user.getId()));
    }

    public void removeSpecialPermission(Long permissionId, Long userId) {
        // check user
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new UserNotFoundException(String.format("User not found with Id = %s", userId));
        }
        User user = userOpt.get();

        // check permission
        Optional<Permission> permissionOpt = permissionRepository.findById(permissionId);
        if (!permissionOpt.isPresent()) {
            throw new PermissionNotFoundException(String.format("Permission not found with Id = %s", permissionId));
        }

        Permission permission = permissionOpt.get();

        user.getSpecial_permissions().remove(permission);

        userRepository.save(user);
        log.info(String.format("Removed special permission %s on user id = %s", permission.getPermission(), user.getId()));
    }

    public Long countUserWithRole(Long roleId) throws RoleNotFoundException {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            throw new RoleNotFoundException(String.format("Role not found with Id = %s", roleId));
        }
        Role role = roleOpt.get();
        return roleRepository.countRoleUsage(roleId);
    }

    public List<UserDTO> filterUserWithUsername(String keyword) {
        List<UserDTO> allUsers = getUserPresentationList();

        return allUsers.stream()
                .filter(user -> user.getUsername().contains(keyword))
                .collect(Collectors.toList());
    }

    public List<UserDTO> filterUserWithRole(String role) {
        List<UserDTO> allUsers = getUserPresentationList();

        return allUsers.stream()
                .filter(user -> user.getRoles().contains(role))
                .collect(Collectors.toList());
    }

    public List<UserDTO> filterUserWithPermission(String permissionKey) {
        List<UserDTO> allUsers = getUserPresentationList();
        Iterable<Role> allRoles = roleService.getRoleList();
        List<String> rolesWithPermission = new ArrayList<>();
        for (Role role : allRoles) {
            for (Permission permission : role.getPermissions()) {
                if (permissionKey.equals(permission.getPermission())) {
                    rolesWithPermission.add(role.getRole());
                    break;
                }
            }
        }
        List<UserDTO> filteredUsers = new ArrayList<>();
        for (UserDTO user : allUsers) {
            for (String userRole : user.getRoles()) {
                if (rolesWithPermission.contains(userRole)) {
                    filteredUsers.add(user);
                    break;
                }
            }
        }
        return filteredUsers;
    }

    public Page<User> filterUser(SearchUserRequest request) {

        List<String> usernames = request.getUsernames();
        List<String> emails = request.getEmails();
        List<String> roles = request.getRoles();
        List<String> permissions = request.getPermissions();
        Integer page = request.getPage();
        Integer limit = request.getLimit();

        Specification<User> conditions = (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            if (usernames != null && !usernames.isEmpty()) {
                predicates.add(root.get(User_.USERNAME).in(usernames));


            }
            System.out.println("Predicates 1: " + predicates);

            if (emails != null && !emails.isEmpty()) {
                predicates.add(root.get(User_.EMAIL).in(emails));
            }
            System.out.println("Predicates 2: " + predicates);
            if (roles != null && !roles.isEmpty()) {
                predicates.add(root.join("roles", JoinType.INNER).get("role").in(roles));
            }

            if (permissions != null && !permissions.isEmpty()) {
                predicates.add(root.join("roles").join("permissions").get("permission").in(permissions));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, limit, Sort.by(User_.ID).descending());

        return userRepository.findAll(conditions, pageable);
    }

    public void followUser(Long followId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principal = (User) authentication.getPrincipal();
        Long myId = principal.getId();
        UserFollow userFollow = new UserFollow();
        userFollow.setUserId(myId);
        userFollow.setFollowId(followId);
        userFollowRepository.save(userFollow);
    }

    public void unfollowUser(Long userId) {

    }
}
