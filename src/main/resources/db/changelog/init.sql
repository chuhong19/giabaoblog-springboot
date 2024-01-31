--liquibase formatted sql

--changeset phuoccm@suga.vn:create-table-roles
CREATE TABLE "roles" (
    "id" bigserial PRIMARY KEY,
    "role" varchar(255) NOT NULL
);

--changeset phuoccm@suga.vn:create-table-permissions
CREATE TABLE "permissions" (
    "id" bigserial PRIMARY KEY,
    "permission" varchar(255) NOT NULL,
    "enabled" boolean DEFAULT true,
    "note" varchar(255)
);

--changeset phuoccm@suga.vn:create-table-relations-roles-permissions
CREATE TABLE "permissions_roles" (
    "role_id" bigint,
    "permission_id" bigint,
    PRIMARY KEY ("role_id", "permission_id"),
    FOREIGN KEY ("role_id") REFERENCES "roles" ("id") ON DELETE CASCADE,
    FOREIGN KEY ("permission_id") REFERENCES "permissions" ("id") ON DELETE CASCADE
);

INSERT INTO permissions(id, permission, note) VALUES (1, 'LOGIN', 'User Login');
INSERT INTO permissions(id, permission, note) VALUES (2, 'VIEW_PROFILE', 'View user profile');
INSERT INTO permissions(id, permission, note) VALUES (3, 'ADMIN_USER_DATA', 'Manage user data');

INSERT INTO permissions(id, permission, note, enabled) VALUES (4, 'ADMIN_STATISTICS', 'View statistical graphs', false);

INSERT INTO roles(id, role) VALUES (1, 'USER');
INSERT INTO roles(id, role) VALUES (2, 'ADMINISTRATOR');

INSERT INTO permissions_roles(permission_id, role_id) VALUES (1, 1);
INSERT INTO permissions_roles(permission_id, role_id) VALUES (2, 1);

INSERT INTO permissions_roles(permission_id, role_id) VALUES (1, 2);
INSERT INTO permissions_roles(permission_id, role_id) VALUES (2, 2);
INSERT INTO permissions_roles(permission_id, role_id) VALUES (3, 2);

--changeset phuoccm@suga.vn:create-table-users
CREATE TABLE "users" (
    "id" bigserial PRIMARY KEY,
    "username" varchar(255) UNIQUE,
    "email" varchar(255) UNIQUE,
    "password" varchar(255),
    "firstname" varchar(255),
    "lastname" varchar(255),
    "gender" varchar(255) CHECK (gender::text = ANY (ARRAY['MALE'::character varying, 'FEMALE'::character varying, 'OTHER'::character varying, 'UNKNOWN'::character varying]::text[])),
    "roles" varchar(255)[],
    "account_non_expired" bool NOT NULL,
    "account_non_locked" bool NOT NULL,
    "credentials_non_expired" bool NOT NULL,
    "enabled" bool NOT NULL,
    "created_at" timestamptz(6),
    "updated_at" timestamptz(6),
    "reset_token" varchar(255)
);

--changeset phuoccm@suga.vn:create-table-users_roles
CREATE TABLE "users_roles" (
    "user_id" bigint,
    "role_id" bigint,
    PRIMARY KEY ("user_id", "role_id"),
    FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
    FOREIGN KEY ("role_id") REFERENCES "roles" ("id") ON DELETE CASCADE
);


--changeset phuoccm@suga.vn:create-table-token
CREATE TABLE "token" (
    "id" bigserial PRIMARY KEY,
    "user_id" int8,
    "expired" bool,
    "revoked" bool,
    "token_type" varchar(255) CHECK (token_type::text = 'BEARER'::text),
    "token" text UNIQUE,
    "created_at" timestamptz(6),
    "updated_at" timestamptz(6)
);

--changeset phuoccm@suga.vn:create-table-user_alias
CREATE TABLE "user_alias" (
    "id" bigserial PRIMARY KEY,
    "user_id" int8 NOT NULL,
    "auth_code" varchar(255),
    "auth_id" varchar(255) NOT NULL,
    "grant_type" varchar(255) NOT NULL,
    "country" varchar(255),
    "confirmed" bool NOT NULL,
    "enabled" bool NOT NULL,
    "auth_code_expires_at" timestamptz(6),
    "confirmed_at" timestamptz(6),
    "created_at" timestamptz(6),
    "updated_at" timestamptz(6)
);


--changeset phuoccm@suga.vn:create-table-users_special_permissions
CREATE TABLE "users_special_permissions" (
    "user_id" bigint,
    "special_permission_id" bigint,
    PRIMARY KEY ("user_id", "special_permission_id"),
    FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
    FOREIGN KEY ("special_permission_id") REFERENCES "permissions" ("id") ON DELETE CASCADE
);

--changeset phuoccm@suga.vn:add-auditable-to-domain-permission
ALTER TABLE "permissions"
    ADD COLUMN created_at timestamptz(6),
    ADD COLUMN updated_at timestamptz(6);

--changeset phuoccm@suga.vn:add-auditable-to-domain-role
ALTER TABLE "roles"
    ADD COLUMN created_at timestamptz(6),
    ADD COLUMN updated_at timestamptz(6);