


SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


COMMENT ON SCHEMA "public" IS 'standard public schema';



CREATE EXTENSION IF NOT EXISTS "pg_stat_statements" WITH SCHEMA "extensions";






CREATE EXTENSION IF NOT EXISTS "pgcrypto" WITH SCHEMA "extensions";






CREATE EXTENSION IF NOT EXISTS "supabase_vault" WITH SCHEMA "vault";






CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA "extensions";






CREATE TYPE "public"."invoice_status_enum" AS ENUM (
    'ISSUED',
    'PAID',
    'CANCELLED'
);


ALTER TYPE "public"."invoice_status_enum" OWNER TO "postgres";


CREATE TYPE "public"."order_status_enum" AS ENUM (
    'DRAFT',
    'CONFIRMED',
    'CANCELLED',
    'COMPLETED'
);


ALTER TYPE "public"."order_status_enum" OWNER TO "postgres";


CREATE TYPE "public"."production_step_status_enum" AS ENUM (
    'PENDING',
    'IN_PROGRESS',
    'DONE'
);


ALTER TYPE "public"."production_step_status_enum" OWNER TO "postgres";


CREATE TYPE "public"."unit_enum" AS ENUM (
    'piece',
    'kg',
    'g',
    'l',
    'ml',
    'm',
    'cm',
    'm2',
    'm3',
    'hour',
    'day',
    'set'
);


ALTER TYPE "public"."unit_enum" OWNER TO "postgres";

SET default_tablespace = '';

SET default_table_access_method = "heap";


CREATE TABLE IF NOT EXISTS "public"."asset" (
    "id" "uuid" NOT NULL,
    "created_at" timestamp(6) with time zone NOT NULL,
    "name" character varying(255),
    "category_id" "uuid"
);


ALTER TABLE "public"."asset" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."auftrag" (
    "id" "uuid" NOT NULL,
    "created_at" timestamp(6) with time zone NOT NULL,
    "kunden_id" "uuid",
    "order_number" character varying(50),
    "status" "public"."order_status_enum" DEFAULT 'DRAFT'::"public"."order_status_enum" NOT NULL,
    "order_date" timestamp without time zone DEFAULT "now"() NOT NULL,
    "expected_date" timestamp without time zone,
    "confirmed_at" timestamp without time zone,
    "cancelled_at" timestamp without time zone,
    "total_amount" numeric(14,2) DEFAULT 0 NOT NULL,
    "notes" "text",
    "created_by" "uuid",
    "updated_at" timestamp without time zone DEFAULT "now"() NOT NULL,
    "deleted_at" timestamp without time zone
);


ALTER TABLE "public"."auftrag" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."auftrag_assets" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "auftrag_id" "uuid" NOT NULL,
    "asset_id" "uuid" NOT NULL,
    "item_name" character varying(255) NOT NULL,
    "unit" "public"."unit_enum" NOT NULL,
    "quantity" numeric(12,3) NOT NULL,
    "unit_price" numeric(12,2) NOT NULL,
    "line_total" numeric(14,2) NOT NULL,
    "notes" "text",
    "created_at" timestamp without time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp without time zone DEFAULT "now"() NOT NULL,
    "supplier_id" "uuid"
);


ALTER TABLE "public"."auftrag_assets" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."category" (
    "id" "uuid" NOT NULL,
    "created_at" timestamp(6) with time zone NOT NULL,
    "description" character varying(255),
    "name" character varying(50)
);


ALTER TABLE "public"."category" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."invoices" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "invoice_number" character varying(50) NOT NULL,
    "auftrag_id" "uuid" NOT NULL,
    "status" "public"."invoice_status_enum" DEFAULT 'ISSUED'::"public"."invoice_status_enum" NOT NULL,
    "issue_date" timestamp without time zone DEFAULT "now"() NOT NULL,
    "due_date" timestamp without time zone,
    "paid_at" timestamp without time zone,
    "cancelled_at" timestamp without time zone,
    "subtotal" numeric(14,2) DEFAULT 0 NOT NULL,
    "tax_amount" numeric(14,2) DEFAULT 0 NOT NULL,
    "total_amount" numeric(14,2) DEFAULT 0 NOT NULL,
    "notes" "text",
    "created_at" timestamp without time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp without time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."invoices" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."kunde" (
    "id" "uuid" NOT NULL,
    "created_at" timestamp(6) with time zone NOT NULL,
    "name" character varying(255),
    "email" character varying(255),
    "phone" character varying(50),
    "tax_id" character varying(50),
    "street" character varying(255),
    "house_number" character varying(20),
    "zip_code" character varying(20),
    "city" character varying(100),
    "state" character varying(100),
    "country" character varying(100),
    "notes" "text",
    "updated_at" timestamp without time zone DEFAULT "now"() NOT NULL,
    "deleted_at" timestamp without time zone
);


ALTER TABLE "public"."kunde" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."lager" (
    "id" "uuid" NOT NULL,
    "created_at" timestamp(6) with time zone NOT NULL,
    "name" character varying(255),
    "description" "text",
    "street" character varying(255),
    "house_number" character varying(20),
    "zip_code" character varying(20),
    "city" character varying(100),
    "state" character varying(100),
    "country" character varying(100),
    "is_active" boolean DEFAULT true NOT NULL,
    "updated_at" timestamp without time zone DEFAULT "now"() NOT NULL,
    "deleted_at" timestamp without time zone
);


ALTER TABLE "public"."lager" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."order_production_steps" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "auftrag_id" "uuid" NOT NULL,
    "auftrag_asset_id" "uuid" NOT NULL,
    "template_step_id" "uuid" NOT NULL,
    "name" character varying(255) NOT NULL,
    "description" "text",
    "step_order" integer NOT NULL,
    "status" "public"."production_step_status_enum" DEFAULT 'PENDING'::"public"."production_step_status_enum" NOT NULL,
    "started_at" timestamp without time zone,
    "completed_at" timestamp without time zone,
    "assigned_to" "uuid",
    "created_at" timestamp without time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp without time zone DEFAULT "now"() NOT NULL
);


ALTER TABLE "public"."order_production_steps" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."production_steps" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "asset_id" "uuid" NOT NULL,
    "name" character varying(255) NOT NULL,
    "description" "text",
    "step_order" integer NOT NULL,
    "is_deleted" boolean DEFAULT false NOT NULL,
    "created_at" timestamp without time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp without time zone DEFAULT "now"() NOT NULL,
    "notes" character varying
);


ALTER TABLE "public"."production_steps" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."stock_movement" (
    "id" "uuid" NOT NULL,
    "created_at" timestamp(6) with time zone NOT NULL,
    "note" character varying(255),
    "quantity" bigint,
    "type" character varying(255),
    "asset_id" "uuid",
    "auftrag_id" "uuid",
    "lager_id" "uuid"
);


ALTER TABLE "public"."stock_movement" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."suppliers" (
    "id" "uuid" DEFAULT "gen_random_uuid"() NOT NULL,
    "name" character varying(255) NOT NULL,
    "email" character varying(255),
    "phone" character varying(50),
    "tax_id" character varying(50),
    "street" character varying(255),
    "house_number" character varying(20),
    "zip_code" character varying(20),
    "city" character varying(100),
    "state" character varying(100),
    "country" character varying(100),
    "notes" "text",
    "created_at" timestamp without time zone DEFAULT "now"() NOT NULL,
    "updated_at" timestamp without time zone DEFAULT "now"() NOT NULL,
    "deleted_at" timestamp without time zone
);


ALTER TABLE "public"."suppliers" OWNER TO "postgres";


ALTER TABLE ONLY "public"."asset"
    ADD CONSTRAINT "asset_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."auftrag_assets"
    ADD CONSTRAINT "auftrag_assets_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."auftrag"
    ADD CONSTRAINT "auftrag_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."category"
    ADD CONSTRAINT "category_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."invoices"
    ADD CONSTRAINT "invoices_invoice_number_key" UNIQUE ("invoice_number");



ALTER TABLE ONLY "public"."invoices"
    ADD CONSTRAINT "invoices_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."kunde"
    ADD CONSTRAINT "kunde_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."lager"
    ADD CONSTRAINT "lager_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."order_production_steps"
    ADD CONSTRAINT "order_production_steps_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."production_steps"
    ADD CONSTRAINT "production_steps_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."stock_movement"
    ADD CONSTRAINT "stock_movement_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."suppliers"
    ADD CONSTRAINT "suppliers_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."stock_movement"
    ADD CONSTRAINT "fk3o59afv763798u2kf1ulchy2g" FOREIGN KEY ("lager_id") REFERENCES "public"."lager"("id");



ALTER TABLE ONLY "public"."auftrag_assets"
    ADD CONSTRAINT "fk_auftrag_assets_supplier" FOREIGN KEY ("supplier_id") REFERENCES "public"."suppliers"("id");



ALTER TABLE ONLY "public"."auftrag_assets"
    ADD CONSTRAINT "fk_auftrag_items_asset" FOREIGN KEY ("asset_id") REFERENCES "public"."asset"("id");



ALTER TABLE ONLY "public"."auftrag_assets"
    ADD CONSTRAINT "fk_auftrag_items_auftrag" FOREIGN KEY ("auftrag_id") REFERENCES "public"."auftrag"("id");



ALTER TABLE ONLY "public"."invoices"
    ADD CONSTRAINT "fk_invoices_order" FOREIGN KEY ("auftrag_id") REFERENCES "public"."auftrag"("id");



ALTER TABLE ONLY "public"."order_production_steps"
    ADD CONSTRAINT "fk_order_production_steps_order" FOREIGN KEY ("auftrag_id") REFERENCES "public"."auftrag"("id");



ALTER TABLE ONLY "public"."order_production_steps"
    ADD CONSTRAINT "fk_order_production_steps_order_item" FOREIGN KEY ("auftrag_asset_id") REFERENCES "public"."auftrag_assets"("id");



ALTER TABLE ONLY "public"."order_production_steps"
    ADD CONSTRAINT "fk_order_production_steps_template_step" FOREIGN KEY ("template_step_id") REFERENCES "public"."production_steps"("id");



ALTER TABLE ONLY "public"."production_steps"
    ADD CONSTRAINT "fk_production_steps_asset" FOREIGN KEY ("asset_id") REFERENCES "public"."asset"("id");



ALTER TABLE ONLY "public"."stock_movement"
    ADD CONSTRAINT "fkclgxe0mjrynpgiaa6k7f379iv" FOREIGN KEY ("auftrag_id") REFERENCES "public"."auftrag"("id");



ALTER TABLE ONLY "public"."asset"
    ADD CONSTRAINT "fke69ydkxgcthslax73274q33fs" FOREIGN KEY ("category_id") REFERENCES "public"."category"("id");



ALTER TABLE ONLY "public"."stock_movement"
    ADD CONSTRAINT "fkkv6aaghrak19carehe6u3aoyg" FOREIGN KEY ("asset_id") REFERENCES "public"."asset"("id");



ALTER TABLE ONLY "public"."auftrag"
    ADD CONSTRAINT "fkt6ib9ib0dhvqskrwh67nsc78" FOREIGN KEY ("kunden_id") REFERENCES "public"."kunde"("id");





ALTER PUBLICATION "supabase_realtime" OWNER TO "postgres";


GRANT USAGE ON SCHEMA "public" TO "postgres";
GRANT USAGE ON SCHEMA "public" TO "anon";
GRANT USAGE ON SCHEMA "public" TO "authenticated";
GRANT USAGE ON SCHEMA "public" TO "service_role";





































































































































































GRANT ALL ON TABLE "public"."asset" TO "anon";
GRANT ALL ON TABLE "public"."asset" TO "authenticated";
GRANT ALL ON TABLE "public"."asset" TO "service_role";



GRANT ALL ON TABLE "public"."auftrag" TO "anon";
GRANT ALL ON TABLE "public"."auftrag" TO "authenticated";
GRANT ALL ON TABLE "public"."auftrag" TO "service_role";



GRANT ALL ON TABLE "public"."auftrag_assets" TO "anon";
GRANT ALL ON TABLE "public"."auftrag_assets" TO "authenticated";
GRANT ALL ON TABLE "public"."auftrag_assets" TO "service_role";



GRANT ALL ON TABLE "public"."category" TO "anon";
GRANT ALL ON TABLE "public"."category" TO "authenticated";
GRANT ALL ON TABLE "public"."category" TO "service_role";



GRANT ALL ON TABLE "public"."invoices" TO "anon";
GRANT ALL ON TABLE "public"."invoices" TO "authenticated";
GRANT ALL ON TABLE "public"."invoices" TO "service_role";



GRANT ALL ON TABLE "public"."kunde" TO "anon";
GRANT ALL ON TABLE "public"."kunde" TO "authenticated";
GRANT ALL ON TABLE "public"."kunde" TO "service_role";



GRANT ALL ON TABLE "public"."lager" TO "anon";
GRANT ALL ON TABLE "public"."lager" TO "authenticated";
GRANT ALL ON TABLE "public"."lager" TO "service_role";



GRANT ALL ON TABLE "public"."order_production_steps" TO "anon";
GRANT ALL ON TABLE "public"."order_production_steps" TO "authenticated";
GRANT ALL ON TABLE "public"."order_production_steps" TO "service_role";



GRANT ALL ON TABLE "public"."production_steps" TO "anon";
GRANT ALL ON TABLE "public"."production_steps" TO "authenticated";
GRANT ALL ON TABLE "public"."production_steps" TO "service_role";



GRANT ALL ON TABLE "public"."stock_movement" TO "anon";
GRANT ALL ON TABLE "public"."stock_movement" TO "authenticated";
GRANT ALL ON TABLE "public"."stock_movement" TO "service_role";



GRANT ALL ON TABLE "public"."suppliers" TO "anon";
GRANT ALL ON TABLE "public"."suppliers" TO "authenticated";
GRANT ALL ON TABLE "public"."suppliers" TO "service_role";









ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES TO "service_role";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS TO "service_role";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES TO "service_role";































drop extension if exists "pg_net";


