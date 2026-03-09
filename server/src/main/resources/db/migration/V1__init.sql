-- Extensions
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS vector;


--
-- PostgreSQL database
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


--
-- Name: bump_candidate_on_user_change(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.bump_candidate_on_user_change() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    update candidate
    set data_version = data_version + 1
    where user_id = coalesce(new.id, old.id);

    return null;
end;
$$;


--
-- Name: bump_candidate_ref_version(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.bump_candidate_ref_version() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    update candidate
    set data_version = data_version + 1
    where id = coalesce(new.candidate_id, old.candidate_id);

    return null;
end;
$$;


--
-- Name: bump_candidate_self_version(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.bump_candidate_self_version() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    -- Note that this logic is different from updates on other tables because updating fields on the
    -- candidate table will change the data_version field on the candidate table, which will trigger
    -- another update - and so on, resulting in an infinite loop.
    -- Only bump version if something other than data_version changed
    if new is distinct from old then
        new.data_version := old.data_version + 1;
    end if;

    -- Return NEW to apply the modified row
    return new;
end;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: audit_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.audit_log (
                                  id bigint NOT NULL,
                                  event_date date NOT NULL,
                                  user_id bigint NOT NULL,
                                  type text NOT NULL,
                                  action text NOT NULL,
                                  object_ref text NOT NULL,
                                  description text NOT NULL
);


--
-- Name: audit_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.audit_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: audit_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.audit_log_id_seq OWNED BY public.audit_log.id;


--
-- Name: batch_job_execution; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_job_execution (
                                            job_execution_id bigint NOT NULL,
                                            version bigint,
                                            job_instance_id bigint NOT NULL,
                                            create_time timestamp without time zone NOT NULL,
                                            start_time timestamp without time zone,
                                            end_time timestamp without time zone,
                                            status character varying(10),
                                            exit_code character varying(2500),
                                            exit_message character varying(2500),
                                            last_updated timestamp without time zone
);


--
-- Name: batch_job_execution_context; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_job_execution_context (
                                                    job_execution_id bigint NOT NULL,
                                                    short_context character varying(2500) NOT NULL,
                                                    serialized_context text
);


--
-- Name: batch_job_execution_params; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_job_execution_params (
                                                   job_execution_id bigint NOT NULL,
                                                   parameter_name character varying(100) NOT NULL,
                                                   parameter_type character varying(100) NOT NULL,
                                                   parameter_value character varying(2500),
                                                   identifying character(1) NOT NULL
);


--
-- Name: batch_job_execution_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.batch_job_execution_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: batch_job_instance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_job_instance (
                                           job_instance_id bigint NOT NULL,
                                           version bigint,
                                           job_name character varying(100) NOT NULL,
                                           job_key character varying(32) NOT NULL
);


--
-- Name: batch_job_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.batch_job_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: batch_step_execution; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_step_execution (
                                             step_execution_id bigint NOT NULL,
                                             version bigint NOT NULL,
                                             step_name character varying(100) NOT NULL,
                                             job_execution_id bigint NOT NULL,
                                             create_time timestamp without time zone NOT NULL,
                                             start_time timestamp without time zone,
                                             end_time timestamp without time zone,
                                             status character varying(10),
                                             commit_count bigint,
                                             read_count bigint,
                                             filter_count bigint,
                                             write_count bigint,
                                             read_skip_count bigint,
                                             write_skip_count bigint,
                                             process_skip_count bigint,
                                             rollback_count bigint,
                                             exit_code character varying(2500),
                                             exit_message character varying(2500),
                                             last_updated timestamp without time zone
);


--
-- Name: batch_step_execution_context; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_step_execution_context (
                                                     step_execution_id bigint NOT NULL,
                                                     short_context character varying(2500) NOT NULL,
                                                     serialized_context text
);


--
-- Name: batch_step_execution_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.batch_step_execution_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate (
                                  id bigint NOT NULL,
                                  candidate_number character varying(16),
                                  user_id bigint,
                                  gender text,
                                  dob date,
                                  phone text,
                                  whatsapp text,
                                  status text NOT NULL,
                                  country_id bigint,
                                  city text,
                                  year_of_arrival integer,
                                  nationalityold_id bigint,
                                  unhcr_status text,
                                  unhcr_number text,
                                  additional_info text,
                                  max_education_level_id bigint,
                                  created_by bigint NOT NULL,
                                  created_date timestamp with time zone NOT NULL,
                                  updated_by bigint,
                                  updated_date timestamp with time zone,
                                  address1 text,
                                  candidate_message text,
                                  preferred_language text,
                                  migration_status text,
                                  sflink text,
                                  folderlink text,
                                  videolink text,
                                  survey_type_id bigint,
                                  survey_comment text,
                                  text_search_id text,
                                  returned_home text,
                                  returned_home_reason text,
                                  visa_issues text,
                                  visa_issues_notes text,
                                  avail_immediate text,
                                  avail_immediate_reason text,
                                  avail_immediate_notes text,
                                  family_move text,
                                  family_move_notes text,
                                  int_recruit_reasons text,
                                  int_recruit_rural text,
                                  return_home_safe text,
                                  work_permit text,
                                  work_permit_desired text,
                                  work_desired text,
                                  asylum_year date,
                                  home_location text,
                                  unhcr_not_reg_status text,
                                  unhcr_file integer,
                                  unhcr_notes text,
                                  unhcr_permission text,
                                  unrwa_number text,
                                  unrwa_notes text,
                                  dest_limit text,
                                  dest_limit_notes text,
                                  crime_convict text,
                                  crime_convict_notes text,
                                  conflict text,
                                  conflict_notes text,
                                  residence_status text,
                                  work_abroad text,
                                  host_entry_legally text,
                                  left_home_notes text,
                                  return_home_future text,
                                  return_home_when text,
                                  resettle_third text,
                                  resettle_third_status text,
                                  host_challenges text,
                                  marital_status text,
                                  partner_registered text,
                                  partner_candidate_id bigint,
                                  partner_edu_level_id bigint,
                                  partner_occupation_id bigint,
                                  partner_english text,
                                  partner_english_level_id bigint,
                                  partner_ielts text,
                                  partner_ielts_score text,
                                  partner_citizenship text,
                                  military_service text,
                                  visa_reject text,
                                  can_drive text,
                                  driving_license text,
                                  driving_license_exp date,
                                  driving_license_country_id bigint,
                                  host_entry_year integer,
                                  english_assessment text,
                                  english_assessment_score_ielts text,
                                  int_recruit_rural_notes text,
                                  work_abroad_notes text,
                                  host_entry_legally_notes text,
                                  visa_reject_notes text,
                                  partner_ielts_yr integer,
                                  partner_edu_level_notes text,
                                  partner_occupation_notes text,
                                  returned_home_reason_no text,
                                  residence_status_notes text,
                                  work_desired_notes text,
                                  left_home_reasons text,
                                  military_wanted text,
                                  military_notes text,
                                  military_start date,
                                  military_end date,
                                  int_recruit_other text,
                                  avail_immediate_job_ops text,
                                  unhcr_registered text,
                                  unrwa_registered text,
                                  birth_country_id bigint,
                                  linked_in_link text,
                                  marital_status_notes text,
                                  work_permit_desired_notes text,
                                  host_entry_year_notes text,
                                  unrwa_file integer,
                                  unrwa_not_reg_status text,
                                  ielts_score numeric,
                                  nationality_id bigint,
                                  health_issues text,
                                  health_issues_notes text,
                                  unhcr_consent text,
                                  shareable_notes text,
                                  shareable_cv_attachment_id bigint,
                                  shareable_doc_attachment_id bigint,
                                  mini_intake date,
                                  full_intake date,
                                  state text,
                                  external_id text,
                                  external_id_source text,
                                  covid_vaccinated text,
                                  covid_vaccinated_status text,
                                  covid_vaccinated_date date,
                                  covid_vaccine_name text,
                                  covid_vaccine_notes text,
                                  folderlink_address text,
                                  folderlink_character text,
                                  folderlink_employer text,
                                  folderlink_identity text,
                                  folderlink_medical text,
                                  folderlink_qualification text,
                                  folderlink_registration text,
                                  media_willingness text,
                                  folderlink_engagement text,
                                  folderlink_experience text,
                                  folderlink_family text,
                                  folderlink_immigration text,
                                  folderlink_language text,
                                  partner_ref text,
                                  rego_ip text,
                                  rego_utm_campaign text,
                                  rego_utm_content text,
                                  rego_utm_medium text,
                                  rego_utm_source text,
                                  rego_utm_term text,
                                  rego_partner_param text,
                                  rego_referrer_param text,
                                  contact_consent_registration boolean DEFAULT false NOT NULL,
                                  contact_consent_partners boolean DEFAULT false NOT NULL,
                                  mini_intake_completed_by bigint,
                                  mini_intake_completed_date timestamp with time zone,
                                  full_intake_completed_by bigint,
                                  full_intake_completed_date timestamp with time zone,
                                  french_assessment text,
                                  french_assessment_score_nclc integer,
                                  arrest_imprison text,
                                  arrest_imprison_notes text,
                                  avail_date date,
                                  potential_duplicate boolean DEFAULT false NOT NULL,
                                  public_id character varying(22),
                                  relocated_address text,
                                  relocated_city text,
                                  relocated_state text,
                                  relocated_country_id bigint,
                                  english_assessment_score_det integer,
                                  muted boolean DEFAULT false NOT NULL,
                                  all_notifications boolean DEFAULT false NOT NULL,
                                  registered_by bigint,
                                  change_password boolean DEFAULT false,
                                  accepted_privacy_policy_id text,
                                  accepted_privacy_policy_date timestamp with time zone,
                                  accepted_privacy_policy_partner_id bigint,
                                  text text,
                                  ts_text tsvector GENERATED ALWAYS AS (to_tsvector('english'::regconfig, text)) STORED,
                                  data_version bigint DEFAULT 0 NOT NULL
);


--
-- Name: candidate_attachment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_attachment (
                                             id bigint NOT NULL,
                                             candidate_id bigint NOT NULL,
                                             type text,
                                             name text,
                                             created_by bigint,
                                             created_date timestamp with time zone,
                                             updated_by bigint,
                                             updated_date timestamp with time zone,
                                             location text NOT NULL,
                                             migrated boolean DEFAULT false NOT NULL,
                                             file_type text,
                                             text_extract text,
                                             cv boolean DEFAULT false NOT NULL,
                                             upload_type text
);


--
-- Name: candidate_attachment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_attachment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_attachment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_attachment_id_seq OWNED BY public.candidate_attachment.id;


--
-- Name: candidate_certification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_certification (
                                                id bigint NOT NULL,
                                                candidate_id bigint NOT NULL,
                                                name text,
                                                institution text,
                                                date_completed date
);


--
-- Name: candidate_certification_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_certification_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_certification_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_certification_id_seq OWNED BY public.candidate_certification.id;


--
-- Name: candidate_citizenship; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_citizenship (
                                              id bigint NOT NULL,
                                              candidate_id bigint NOT NULL,
                                              nationality_id bigint,
                                              has_passport text,
                                              notes text,
                                              passport_exp date
);


--
-- Name: candidate_citizenship_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_citizenship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_citizenship_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_citizenship_id_seq OWNED BY public.candidate_citizenship.id;


--
-- Name: candidate_coupon_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_coupon_code (
                                              id bigint NOT NULL,
                                              offer_to_assist_id bigint,
                                              candidate_id bigint NOT NULL,
                                              coupon_code text
);


--
-- Name: candidate_coupon_code_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_coupon_code_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_coupon_code_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_coupon_code_id_seq OWNED BY public.candidate_coupon_code.id;


--
-- Name: candidate_dependant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_dependant (
                                            id bigint NOT NULL,
                                            candidate_id bigint NOT NULL,
                                            relation text,
                                            dob date,
                                            health_concern text,
                                            health_notes text,
                                            name text,
                                            registered text,
                                            relation_other text,
                                            registered_number text,
                                            registered_notes text,
                                            gender text
);


--
-- Name: candidate_dependant_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_dependant_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_dependant_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_dependant_id_seq OWNED BY public.candidate_dependant.id;


--
-- Name: candidate_destination; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_destination (
                                              id bigint NOT NULL,
                                              candidate_id bigint NOT NULL,
                                              country_id bigint,
                                              interest text,
                                              family text,
                                              location text,
                                              notes text
);


--
-- Name: candidate_destination_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_destination_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_destination_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_destination_id_seq OWNED BY public.candidate_destination.id;


--
-- Name: candidate_education; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_education (
                                            id bigint NOT NULL,
                                            candidate_id bigint NOT NULL,
                                            education_type text,
                                            country_id bigint NOT NULL,
                                            length_of_course_years integer,
                                            institution text,
                                            course_name text,
                                            year_completed integer,
                                            major_id bigint,
                                            incomplete boolean
);


--
-- Name: candidate_education_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_education_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_education_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_education_id_seq OWNED BY public.candidate_education.id;


--
-- Name: candidate_exam; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_exam (
                                       id bigint NOT NULL,
                                       candidate_id bigint NOT NULL,
                                       exam text,
                                       other_exam text,
                                       score text,
                                       year integer,
                                       notes text
);


--
-- Name: candidate_exam_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_exam_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_exam_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_exam_id_seq OWNED BY public.candidate_exam.id;


--
-- Name: candidate_form; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_form (
                                       id bigint NOT NULL,
                                       name text,
                                       description text
);


--
-- Name: candidate_form_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_form_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_form_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_form_id_seq OWNED BY public.candidate_form.id;


--
-- Name: candidate_form_instance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_form_instance (
                                                candidate_id bigint NOT NULL,
                                                form_id bigint NOT NULL,
                                                created_date timestamp with time zone NOT NULL,
                                                updated_date timestamp with time zone
);


--
-- Name: candidate_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_id_seq OWNED BY public.candidate.id;


--
-- Name: candidate_job_experience; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_job_experience (
                                                 id bigint NOT NULL,
                                                 candidate_id bigint NOT NULL,
                                                 candidate_occupation_id bigint NOT NULL,
                                                 company_name text,
                                                 country_id bigint,
                                                 role text,
                                                 start_date date,
                                                 end_date date,
                                                 full_time boolean,
                                                 paid boolean,
                                                 description text,
                                                 ts tsvector GENERATED ALWAYS AS (to_tsvector('english'::regconfig, description)) STORED
);


--
-- Name: candidate_job_experience_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_job_experience_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_job_experience_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_job_experience_id_seq OWNED BY public.candidate_job_experience.id;


--
-- Name: candidate_json_cache; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_json_cache (
                                             candidate_id bigint NOT NULL,
                                             data_version bigint NOT NULL,
                                             "json" jsonb NOT NULL,
                                             computed_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: candidate_language; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_language (
                                           id bigint NOT NULL,
                                           candidate_id bigint NOT NULL,
                                           language_id bigint,
                                           written_level_id bigint,
                                           spoken_level_id bigint
);


--
-- Name: candidate_language_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_language_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_language_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_language_id_seq OWNED BY public.candidate_language.id;


--
-- Name: candidate_note; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_note (
                                       id bigint NOT NULL,
                                       candidate_id bigint NOT NULL,
                                       note_type text NOT NULL,
                                       title text NOT NULL,
                                       comment text,
                                       created_by bigint,
                                       created_date timestamp with time zone,
                                       updated_by bigint,
                                       updated_date timestamp with time zone
);


--
-- Name: candidate_note_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_note_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_note_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_note_id_seq OWNED BY public.candidate_note.id;


--
-- Name: candidate_occupation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_occupation (
                                             id bigint NOT NULL,
                                             candidate_id bigint NOT NULL,
                                             occupation_id bigint,
                                             years_experience integer NOT NULL,
                                             top_candidate boolean,
                                             migration_occupation text,
                                             updated_by bigint,
                                             updated_date timestamp with time zone,
                                             created_by bigint,
                                             created_date timestamp with time zone
);


--
-- Name: candidate_occupation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_occupation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_occupation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_occupation_id_seq OWNED BY public.candidate_occupation.id;


--
-- Name: candidate_opportunity; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_opportunity (
                                              id bigint NOT NULL,
                                              candidate_id bigint,
                                              closing_comments_for_candidate text,
                                              employer_feedback text,
                                              job_opp_id bigint,
                                              stage text,
                                              closing_comments text,
                                              closed boolean DEFAULT false NOT NULL,
                                              name text,
                                              next_step text,
                                              next_step_due_date date,
                                              sf_id text,
                                              stage_order integer,
                                              created_by bigint,
                                              created_date timestamp with time zone,
                                              updated_by bigint,
                                              updated_date timestamp with time zone,
                                              won boolean DEFAULT false NOT NULL,
                                              file_offer_link text,
                                              file_offer_name text,
                                              last_active_stage text DEFAULT 'prospect'::text,
                                              relocating_dependant_ids text
);


--
-- Name: candidate_opportunity_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_opportunity_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_opportunity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_opportunity_id_seq OWNED BY public.candidate_opportunity.id;


--
-- Name: candidate_property; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_property (
                                           candidate_id bigint NOT NULL,
                                           name text NOT NULL,
                                           value text,
                                           related_task_assignment_id bigint
);


--
-- Name: candidate_property_definition; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_property_definition (
                                                      id bigint NOT NULL,
                                                      name text NOT NULL,
                                                      label text,
                                                      definition text,
                                                      type text
);


--
-- Name: candidate_property_definition_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_property_definition_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_property_definition_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_property_definition_id_seq OWNED BY public.candidate_property_definition.id;


--
-- Name: candidate_review_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_review_item (
                                              id bigint NOT NULL,
                                              candidate_id bigint NOT NULL,
                                              saved_search_id bigint NOT NULL,
                                              review_status text NOT NULL,
                                              comment text,
                                              created_by bigint,
                                              created_date timestamp with time zone,
                                              updated_by bigint,
                                              updated_date timestamp with time zone
);


--
-- Name: candidate_review_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_review_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_review_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_review_item_id_seq OWNED BY public.candidate_review_item.id;


--
-- Name: candidate_saved_list; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_saved_list (
                                             candidate_id bigint NOT NULL,
                                             saved_list_id bigint NOT NULL,
                                             context_note text,
                                             shareable_cv_attachment_id bigint,
                                             shareable_doc_attachment_id bigint
);


--
-- Name: candidate_skill; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_skill (
                                        id bigint NOT NULL,
                                        candidate_id bigint NOT NULL,
                                        skill text,
                                        time_period character varying(100)
);


--
-- Name: candidate_skill_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_skill_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_skill_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_skill_id_seq OWNED BY public.candidate_skill.id;


--
-- Name: candidate_visa_check; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_visa_check (
                                             id bigint NOT NULL,
                                             candidate_id bigint NOT NULL,
                                             country_id bigint,
                                             created_by bigint,
                                             created_date timestamp with time zone,
                                             updated_by bigint,
                                             updated_date timestamp with time zone,
                                             protection text,
                                             protection_grounds text,
                                             health_assessment text,
                                             health_assessment_notes text,
                                             character_assessment text,
                                             character_assessment_notes text,
                                             security_risk text,
                                             security_risk_notes text,
                                             valid_travel_docs text,
                                             valid_travel_docs_notes text,
                                             overall_risk text,
                                             overall_risk_notes text,
                                             english_threshold text,
                                             english_threshold_notes text,
                                             pathway_assessment text,
                                             pathway_assessment_notes text,
                                             destination_family text,
                                             destination_family_location text
);


--
-- Name: candidate_visa_check_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_visa_check_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_visa_check_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_visa_check_id_seq OWNED BY public.candidate_visa_check.id;


--
-- Name: candidate_visa_job_check; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_visa_job_check (
                                                 id bigint NOT NULL,
                                                 interest text,
                                                 interest_notes text,
                                                 occupation_id bigint,
                                                 occupation_notes text,
                                                 salary_tsmit text,
                                                 regional text,
                                                 eligible_494 text,
                                                 eligible_494_notes text,
                                                 eligible_186 text,
                                                 eligible_186_notes text,
                                                 eligible_other text,
                                                 eligible_other_notes text,
                                                 put_forward text,
                                                 notes text,
                                                 candidate_visa_check_id bigint,
                                                 tbb_eligibility text,
                                                 qualification text,
                                                 qualification_notes text,
                                                 job_opp_id bigint,
                                                 relevant_work_exp text,
                                                 age_requirement text,
                                                 preferred_pathways text,
                                                 ineligible_pathways text,
                                                 eligible_pathways text,
                                                 occupation_category text,
                                                 occupation_sub_category text,
                                                 english_threshold text,
                                                 languages_threshold_notes text,
                                                 languages_required text,
                                                 languages_threshold_met text,
                                                 candidate_id bigint NOT NULL
);


--
-- Name: candidate_visa_job_check_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_visa_job_check_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_visa_job_check_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_visa_job_check_id_seq OWNED BY public.candidate_visa_job_check.id;


--
-- Name: chat_post; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_post (
                                  id bigint NOT NULL,
                                  content text,
                                  job_chat_id bigint NOT NULL,
                                  created_by bigint NOT NULL,
                                  created_date timestamp with time zone NOT NULL,
                                  updated_by bigint,
                                  updated_date timestamp with time zone
);


--
-- Name: chat_post_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.chat_post_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: chat_post_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.chat_post_id_seq OWNED BY public.chat_post.id;


--
-- Name: chatbot_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chatbot_message (
                                        id uuid NOT NULL,
                                        session_id uuid NOT NULL,
                                        question_id uuid NOT NULL,
                                        sender character varying(255) NOT NULL,
                                        message text NOT NULL,
                                        "timestamp" timestamp with time zone NOT NULL,
                                        referenced_faq_ids jsonb,
                                        CONSTRAINT chatbot_message_sender_check CHECK (((sender)::text = ANY ((ARRAY['USER'::character varying, 'BOT'::character varying])::text[])))
);


--
-- Name: COLUMN chatbot_message.referenced_faq_ids; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.chatbot_message.referenced_faq_ids IS 'Array of FAQ IDs that were referenced in generating this bot response. Used for tracking which FAQs are most commonly used.';


--
-- Name: country; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.country (
                                id bigint NOT NULL,
                                name text NOT NULL,
                                status text DEFAULT 'active'::text NOT NULL,
                                iso_code text
);


--
-- Name: country_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.country_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: country_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.country_id_seq OWNED BY public.country.id;


--
-- Name: country_nationality_join; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.country_nationality_join (
                                                 country_id bigint NOT NULL,
                                                 nationality_id bigint NOT NULL
);


--
-- Name: duolingo_coupon; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.duolingo_coupon (
                                        id bigint NOT NULL,
                                        coupon_code character varying(255) NOT NULL,
                                        candidate_id bigint,
                                        expiration_date timestamp with time zone NOT NULL,
                                        date_sent timestamp with time zone,
                                        coupon_status character varying(50) NOT NULL,
                                        test_type character varying(20)
);


--
-- Name: duolingo_coupon_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.duolingo_coupon_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: duolingo_coupon_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.duolingo_coupon_id_seq OWNED BY public.duolingo_coupon.id;


--
-- Name: duolingo_extra_fields; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.duolingo_extra_fields (
                                              id bigint NOT NULL,
                                              certificate_url character varying(255),
                                              interview_url character varying(255),
                                              verification_date character varying(255),
                                              percent_score integer NOT NULL,
                                              scale integer NOT NULL,
                                              literacy_subscore integer NOT NULL,
                                              conversation_subscore integer NOT NULL,
                                              comprehension_subscore integer NOT NULL,
                                              production_subscore integer NOT NULL,
                                              candidate_exam_id bigint NOT NULL
);


--
-- Name: duolingo_extra_fields_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.duolingo_extra_fields_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: duolingo_extra_fields_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.duolingo_extra_fields_id_seq OWNED BY public.duolingo_extra_fields.id;


--
-- Name: education_level; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.education_level (
                                        id bigint NOT NULL,
                                        name text NOT NULL,
                                        level integer NOT NULL,
                                        status text DEFAULT 'active'::text NOT NULL,
                                        education_type text,
                                        isced_code character varying(20)
);


--
-- Name: education_level_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.education_level_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: education_level_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.education_level_id_seq OWNED BY public.education_level.id;


--
-- Name: education_major; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.education_major (
                                        id bigint NOT NULL,
                                        name text NOT NULL,
                                        status text DEFAULT 'active'::text NOT NULL
);


--
-- Name: education_major_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.education_major_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: education_major_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.education_major_id_seq OWNED BY public.education_major.id;


--
-- Name: employer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employer (
                                 id bigint NOT NULL,
                                 country_id bigint,
                                 description text,
                                 has_hired_internationally boolean,
                                 name text,
                                 sf_id text,
                                 website text,
                                 created_by bigint,
                                 created_date timestamp with time zone,
                                 updated_by bigint,
                                 updated_date timestamp with time zone
);


--
-- Name: employer_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employer_id_seq OWNED BY public.employer.id;


--
-- Name: export_column; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.export_column (
                                      id bigint NOT NULL,
                                      saved_list_id bigint,
                                      saved_search_id bigint,
                                      index integer NOT NULL,
                                      key text,
                                      properties text
);


--
-- Name: export_column_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.export_column_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: export_column_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.export_column_id_seq OWNED BY public.export_column.id;


--
-- Name: help_link; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.help_link (
                                  id bigint NOT NULL,
                                  country_id bigint,
                                  case_stage text,
                                  job_stage text,
                                  label text NOT NULL,
                                  link text NOT NULL,
                                  focus text,
                                  next_step_name text,
                                  next_step_text text,
                                  next_step_days integer,
                                  created_by bigint,
                                  created_date timestamp with time zone,
                                  updated_by bigint,
                                  updated_date timestamp with time zone
);


--
-- Name: help_link_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.help_link_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: help_link_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.help_link_id_seq OWNED BY public.help_link.id;


--
-- Name: industry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.industry (
                                 id bigint NOT NULL,
                                 name text NOT NULL,
                                 status text DEFAULT 'active'::text NOT NULL
);


--
-- Name: industry_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.industry_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: industry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.industry_id_seq OWNED BY public.industry.id;


--
-- Name: job_chat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.job_chat (
                                 id bigint NOT NULL,
                                 created_by bigint NOT NULL,
                                 created_date timestamp with time zone NOT NULL,
                                 job_id bigint,
                                 updated_by bigint,
                                 updated_date timestamp with time zone,
                                 type text,
                                 source_partner_id bigint,
                                 candidate_id bigint
);


--
-- Name: job_chat_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.job_chat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: job_chat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.job_chat_id_seq OWNED BY public.job_chat.id;


--
-- Name: job_chat_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.job_chat_user (
                                      job_chat_id bigint NOT NULL,
                                      user_id bigint NOT NULL,
                                      last_read_post_id bigint
);


--
-- Name: job_opp_intake; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.job_opp_intake (
                                       id bigint NOT NULL,
                                       recruitment_process text,
                                       employer_cost_commitment text,
                                       location text,
                                       location_details text,
                                       salary_range text,
                                       benefits text,
                                       language_requirements text,
                                       employment_experience text,
                                       education_requirements text,
                                       skill_requirements text,
                                       occupation_code text,
                                       min_salary text,
                                       visa_pathways text
);


--
-- Name: job_opp_intake_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.job_opp_intake_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: job_opp_intake_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.job_opp_intake_id_seq OWNED BY public.job_opp_intake.id;


--
-- Name: job_suggested_saved_search; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.job_suggested_saved_search (
                                                   tc_job_id bigint NOT NULL,
                                                   saved_search_id bigint NOT NULL
);


--
-- Name: language; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.language (
                                 id bigint NOT NULL,
                                 name text NOT NULL,
                                 status text DEFAULT 'active'::text NOT NULL,
                                 iso_code text
);


--
-- Name: language_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.language_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: language_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.language_id_seq OWNED BY public.language.id;


--
-- Name: language_level; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.language_level (
                                       id bigint NOT NULL,
                                       name text NOT NULL,
                                       status text DEFAULT 'active'::text NOT NULL,
                                       level integer NOT NULL,
                                       cefr_level character varying(20)
);


--
-- Name: language_level_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.language_level_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: language_level_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.language_level_id_seq OWNED BY public.language_level.id;


--
-- Name: link_preview; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.link_preview (
                                     id bigint NOT NULL,
                                     chat_post_id bigint,
                                     url text,
                                     title text,
                                     description text,
                                     image_url text,
                                     domain text,
                                     favicon_url text
);


--
-- Name: link_preview_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.link_preview_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: link_preview_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.link_preview_id_seq OWNED BY public.link_preview.id;


--
-- Name: nationality; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.nationality (
                                    id bigint NOT NULL,
                                    name text NOT NULL,
                                    status text DEFAULT 'active'::text NOT NULL
);


--
-- Name: nationality_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.nationality_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: nationality_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.nationality_id_seq OWNED BY public.nationality.id;


--
-- Name: occupation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occupation (
                                   id bigint NOT NULL,
                                   name text NOT NULL,
                                   status text DEFAULT 'active'::text NOT NULL,
                                   isco08_code text
);


--
-- Name: occupation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occupation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: occupation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.occupation_id_seq OWNED BY public.occupation.id;


--
-- Name: offer_to_assist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.offer_to_assist (
                                        id bigint NOT NULL,
                                        created_by bigint NOT NULL,
                                        created_date timestamp with time zone NOT NULL,
                                        updated_by bigint,
                                        updated_date timestamp with time zone,
                                        additional_notes text,
                                        partner_id bigint NOT NULL,
                                        public_id character varying(22),
                                        reason text
);


--
-- Name: offer_to_assist_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.offer_to_assist_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: offer_to_assist_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.offer_to_assist_id_seq OWNED BY public.offer_to_assist.id;


--
-- Name: partner; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.partner (
                                default_source_partner boolean DEFAULT false NOT NULL,
                                id bigint NOT NULL,
                                logo text,
                                name text NOT NULL,
                                status text NOT NULL,
                                registration_landing_page text,
                                registration_domain text,
                                website_url text,
                                abbreviation text NOT NULL,
                                notification_email text,
                                default_partner_ref boolean DEFAULT false,
                                sflink text,
                                auto_assignable boolean DEFAULT false NOT NULL,
                                default_contact_id bigint,
                                default_job_creator boolean DEFAULT false NOT NULL,
                                source_partner boolean DEFAULT false NOT NULL,
                                job_creator boolean DEFAULT false NOT NULL,
                                employer_id bigint,
                                redirect_partner_id bigint,
                                public_api_key_hash character varying(255),
                                public_api_authorities text,
                                public_id character varying(22),
                                accepted_data_processing_agreement_id character varying(255),
                                accepted_data_processing_agreement_date timestamp with time zone,
                                first_dpa_seen_date timestamp with time zone
);


--
-- Name: partner_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.partner_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: partner_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.partner_id_seq OWNED BY public.partner.id;


--
-- Name: partner_job; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.partner_job (
                                    partner_id bigint NOT NULL,
                                    tc_job_id bigint NOT NULL,
                                    contact_id bigint
);


--
-- Name: partner_source_country; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.partner_source_country (
                                               partner_id bigint NOT NULL,
                                               country_id bigint NOT NULL
);


--
-- Name: reaction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reaction (
                                 id bigint NOT NULL,
                                 chat_post_id bigint,
                                 emoji text
);


--
-- Name: reaction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.reaction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: reaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.reaction_id_seq OWNED BY public.reaction.id;


--
-- Name: reaction_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reaction_user (
                                      reaction_id bigint NOT NULL,
                                      user_id bigint NOT NULL
);


--
-- Name: root_request; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.root_request (
                                     id bigint NOT NULL,
                                     ip_address text,
                                     partner_abbreviation text,
                                     query_string text,
                                     request_url text,
                                     "timestamp" timestamp without time zone,
                                     utm_campaign text,
                                     utm_content text,
                                     utm_medium text,
                                     utm_source text,
                                     utm_term text,
                                     referrer_param text
);


--
-- Name: root_request_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.root_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: root_request_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.root_request_id_seq OWNED BY public.root_request.id;


--
-- Name: salesforce_job_opp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.salesforce_job_opp (
                                           sf_id text,
                                           closed boolean DEFAULT false NOT NULL,
                                           employer text,
                                           name text,
                                           stage text,
                                           stage_order integer,
                                           account_id text,
                                           owner_id text,
                                           id bigint NOT NULL,
                                           submission_due_date timestamp with time zone,
                                           contact_user_id bigint,
                                           job_summary text,
                                           recruiter_partner_id bigint,
                                           suggested_list_id bigint,
                                           submission_list_id bigint,
                                           created_by bigint,
                                           created_date timestamp with time zone,
                                           published_by bigint,
                                           published_date timestamp with time zone,
                                           updated_by bigint,
                                           updated_date timestamp with time zone,
                                           exclusion_list_id bigint,
                                           description text,
                                           salary_range text,
                                           location text,
                                           location_details text,
                                           benefits text,
                                           language_requirements text,
                                           employment_experience text,
                                           education_requirements text,
                                           skill_requirements text,
                                           occupation_code text,
                                           country_object_id bigint,
                                           job_opp_intake_id bigint,
                                           hiring_commitment text,
                                           employer_website text,
                                           employer_hired_internationally text,
                                           opportunity_score text,
                                           won boolean DEFAULT false NOT NULL,
                                           employer_description text,
                                           closing_comments text,
                                           next_step text,
                                           next_step_due_date date,
                                           employer_id bigint,
                                           evergreen boolean DEFAULT false NOT NULL,
                                           evergreen_child_id bigint,
                                           skip_candidate_search boolean DEFAULT false NOT NULL,
                                           jd_file_text text
);


--
-- Name: salesforce_job_opp_tc_job_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.salesforce_job_opp_tc_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: salesforce_job_opp_tc_job_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.salesforce_job_opp_tc_job_id_seq OWNED BY public.salesforce_job_opp.id;


--
-- Name: saved_list; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.saved_list (
                                   id bigint NOT NULL,
                                   status text NOT NULL,
                                   name text NOT NULL,
                                   fixed boolean DEFAULT false NOT NULL,
                                   watcher_ids text,
                                   created_by bigint NOT NULL,
                                   created_date timestamp with time zone NOT NULL,
                                   updated_by bigint,
                                   updated_date timestamp with time zone,
                                   saved_search_id bigint,
                                   global boolean DEFAULT false NOT NULL,
                                   saved_search_source_id bigint,
                                   displayed_fields_long text,
                                   displayed_fields_short text,
                                   folderlink text,
                                   foldercvlink text,
                                   folderjdlink text,
                                   registered_job boolean DEFAULT false NOT NULL,
                                   description text,
                                   published_doc_link text,
                                   tc_short_name text,
                                   sf_opp_is_closed boolean DEFAULT false NOT NULL,
                                   job_id bigint,
                                   file_jd_name text,
                                   file_jd_link text,
                                   file_joi_name text,
                                   file_joi_link text,
                                   file_interview_guidance_name text,
                                   file_interview_guidance_link text,
                                   file_mou_name text,
                                   file_mou_link text,
                                   public_id character varying(22)
);


--
-- Name: saved_list_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.saved_list_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: saved_list_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.saved_list_id_seq OWNED BY public.saved_list.id;


--
-- Name: saved_search; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.saved_search (
                                     id bigint NOT NULL,
                                     status text NOT NULL,
                                     name text NOT NULL,
                                     keyword text,
                                     statuses text,
                                     gender text,
                                     occupation_ids text,
                                     or_profile_keyword text,
                                     verified_occupation_ids text,
                                     verified_occupation_search_type text,
                                     nationality_ids text,
                                     nationality_search_type text,
                                     country_ids text,
                                     other_language_id bigint,
                                     un_registered boolean,
                                     last_modified_from date,
                                     last_modified_to date,
                                     created_from date,
                                     created_to date,
                                     min_age integer,
                                     max_age integer,
                                     education_major_ids text,
                                     created_by bigint NOT NULL,
                                     created_date timestamp with time zone NOT NULL,
                                     updated_by bigint,
                                     updated_date timestamp with time zone,
                                     min_education_level integer,
                                     english_min_spoken_level integer,
                                     english_min_written_level integer,
                                     other_min_spoken_level integer,
                                     other_min_written_level integer,
                                     type text,
                                     fixed boolean DEFAULT false NOT NULL,
                                     reviewable boolean DEFAULT false NOT NULL,
                                     watcher_ids text,
                                     global boolean DEFAULT false NOT NULL,
                                     default_search boolean DEFAULT false NOT NULL,
                                     simple_query_string text,
                                     min_yrs integer,
                                     max_yrs integer,
                                     displayed_fields_long text,
                                     displayed_fields_short text,
                                     default_save_selection_list_id bigint,
                                     description text,
                                     survey_type_ids text,
                                     exclusion_list_id bigint,
                                     partner_ids text,
                                     job_id bigint,
                                     country_search_type text,
                                     rego_referrer_param text,
                                     any_opps boolean,
                                     closed_opps boolean,
                                     relocated_opps boolean,
                                     mini_intake_completed boolean,
                                     full_intake_completed boolean,
                                     unhcr_statuses text,
                                     list_all_ids text,
                                     list_all_search_type text,
                                     list_any_ids text,
                                     list_any_search_type text,
                                     potential_duplicate boolean,
                                     public_id character varying(22),
                                     include_pending_terms_candidates boolean,
                                     candidate_numbers text
);


--
-- Name: saved_search_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.saved_search_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: saved_search_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.saved_search_id_seq OWNED BY public.saved_search.id;


--
-- Name: search_join; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.search_join (
                                    id bigint NOT NULL,
                                    search_id bigint NOT NULL,
                                    child_search_id bigint NOT NULL,
                                    search_type text NOT NULL
);


--
-- Name: search_join_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.search_join_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: search_join_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.search_join_id_seq OWNED BY public.search_join.id;


--
-- Name: service_assignment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service_assignment (
                                           id bigint NOT NULL,
                                           provider character varying(255) NOT NULL,
                                           service_code character varying(255) NOT NULL,
                                           resource_id bigint,
                                           candidate_id bigint NOT NULL,
                                           actor_id bigint,
                                           status character varying(255) NOT NULL,
                                           assigned_at timestamp with time zone NOT NULL,
                                           created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: service_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.service_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: service_assignment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.service_assignment_id_seq OWNED BY public.service_assignment.id;


--
-- Name: service_resource; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service_resource (
                                         id bigint NOT NULL,
                                         provider character varying(255) NOT NULL,
                                         service_code character varying(255) NOT NULL,
                                         resource_code character varying(255),
                                         status character varying(255) NOT NULL,
                                         expires_at timestamp with time zone,
                                         sent_at timestamp with time zone,
                                         created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: service_resource_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.service_resource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: service_resource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.service_resource_id_seq OWNED BY public.service_resource.id;


--
-- Name: shedlock; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.shedlock (
                                 name character varying(64) NOT NULL,
                                 lock_until timestamp(3) without time zone,
                                 locked_at timestamp(3) without time zone,
                                 locked_by character varying(255)
);


--
-- Name: skills_esco_en; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.skills_esco_en (
                                       concepttype text,
                                       concepturi text NOT NULL,
                                       skilltype text,
                                       reuselevel text,
                                       preferredlabel text,
                                       altlabels text,
                                       hiddenlabels text,
                                       status text,
                                       modifieddate text,
                                       scopenote text,
                                       definition text,
                                       inscheme text,
                                       description text
);


--
-- Name: skills_tech_onet_en; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.skills_tech_onet_en (
                                            id bigint NOT NULL,
                                            "O*NET-SOC CODE" text,
                                            example text,
                                            "Commodity Code" text,
                                            "Commodity Title" text,
                                            "Hot Technology" text,
                                            "In Demand" text
);


--
-- Name: skills_tech_onet_en_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.skills_tech_onet_en_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: skills_tech_onet_en_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.skills_tech_onet_en_id_seq OWNED BY public.skills_tech_onet_en.id;


--
-- Name: survey_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.survey_type (
                                    id bigint NOT NULL,
                                    name text NOT NULL,
                                    status text DEFAULT 'active'::text NOT NULL
);


--
-- Name: survey_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.survey_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: survey_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.survey_type_id_seq OWNED BY public.survey_type.id;


--
-- Name: system_language; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.system_language (
                                        id bigint NOT NULL,
                                        language text NOT NULL,
                                        label text NOT NULL,
                                        status text DEFAULT 'active'::text NOT NULL,
                                        created_by bigint,
                                        created_date timestamp with time zone,
                                        updated_by bigint,
                                        updated_date timestamp with time zone
);


--
-- Name: system_language_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.system_language_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: system_language_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.system_language_id_seq OWNED BY public.system_language.id;


--
-- Name: task; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task (
                             id bigint NOT NULL,
                             admin boolean DEFAULT false NOT NULL,
                             created_by bigint NOT NULL,
                             created_date timestamp with time zone NOT NULL,
                             days_to_complete integer,
                             description text,
                             doc_link text,
                             name text NOT NULL,
                             optional boolean DEFAULT false NOT NULL,
                             task_type text,
                             updated_by bigint,
                             updated_date timestamp with time zone,
                             upload_subfolder_name text,
                             upload_type text,
                             uploadable_file_types text,
                             candidate_answer_field text,
                             display_name text,
                             explicit_allowed_answers text,
                             required_metadata jsonb,
                             candidate_form_id bigint
);


--
-- Name: task_assignment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_assignment (
                                        id bigint NOT NULL,
                                        abandoned_date timestamp with time zone,
                                        activated_by bigint NOT NULL,
                                        activated_date timestamp with time zone NOT NULL,
                                        candidate_id bigint NOT NULL,
                                        candidate_notes text,
                                        completed_date timestamp with time zone,
                                        deactivated_by bigint,
                                        deactivated_date timestamp with time zone,
                                        due_date timestamp with time zone,
                                        related_list_id bigint,
                                        status text,
                                        task_id bigint NOT NULL,
                                        task_type text
);


--
-- Name: task_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_assignment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_assignment_id_seq OWNED BY public.task_assignment.id;


--
-- Name: task_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_id_seq OWNED BY public.task.id;


--
-- Name: task_saved_list; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_saved_list (
                                        task_id bigint NOT NULL,
                                        saved_list_id bigint NOT NULL
);


--
-- Name: translation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.translation (
                                    id bigint NOT NULL,
                                    object_id bigint NOT NULL,
                                    object_type text NOT NULL,
                                    language text NOT NULL,
                                    value text NOT NULL,
                                    created_by bigint,
                                    created_date timestamp with time zone,
                                    updated_by bigint,
                                    updated_date timestamp with time zone
);


--
-- Name: translation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.translation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: translation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.translation_id_seq OWNED BY public.translation.id;


--
-- Name: user_job; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_job (
                                 user_id bigint NOT NULL,
                                 tc_job_id bigint NOT NULL
);


--
-- Name: user_saved_list; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_saved_list (
                                        user_id bigint NOT NULL,
                                        saved_list_id bigint NOT NULL
);


--
-- Name: user_saved_search; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_saved_search (
                                          user_id bigint NOT NULL,
                                          saved_search_id bigint NOT NULL
);


--
-- Name: user_source_country; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_source_country (
                                            user_id bigint NOT NULL,
                                            country_id bigint NOT NULL
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
                              id bigint NOT NULL,
                              username text,
                              first_name text,
                              last_name text,
                              email text,
                              role text NOT NULL,
                              status text NOT NULL,
                              password_enc text,
                              last_login timestamp with time zone,
                              created_by bigint,
                              created_date timestamp with time zone,
                              updated_by bigint,
                              updated_date timestamp with time zone,
                              reset_token character varying(100),
                              reset_token_issued_date timestamp without time zone,
                              password_updated_date timestamp without time zone,
                              read_only boolean DEFAULT false NOT NULL,
                              using_mfa boolean DEFAULT false NOT NULL,
                              mfa_secret text,
                              host_domain text,
                              partner_id bigint NOT NULL,
                              approver_id bigint,
                              purpose text,
                              job_creator boolean DEFAULT false NOT NULL,
                              email_verified boolean DEFAULT false,
                              email_verification_token character varying(255),
                              email_verification_token_issued_time timestamp without time zone
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: audit_log id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_log ALTER COLUMN id SET DEFAULT nextval('public.audit_log_id_seq'::regclass);


--
-- Name: candidate id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate ALTER COLUMN id SET DEFAULT nextval('public.candidate_id_seq'::regclass);


--
-- Name: candidate_attachment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_attachment ALTER COLUMN id SET DEFAULT nextval('public.candidate_attachment_id_seq'::regclass);


--
-- Name: candidate_certification id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_certification ALTER COLUMN id SET DEFAULT nextval('public.candidate_certification_id_seq'::regclass);


--
-- Name: candidate_citizenship id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_citizenship ALTER COLUMN id SET DEFAULT nextval('public.candidate_citizenship_id_seq'::regclass);


--
-- Name: candidate_coupon_code id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_coupon_code ALTER COLUMN id SET DEFAULT nextval('public.candidate_coupon_code_id_seq'::regclass);


--
-- Name: candidate_dependant id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_dependant ALTER COLUMN id SET DEFAULT nextval('public.candidate_dependant_id_seq'::regclass);


--
-- Name: candidate_destination id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_destination ALTER COLUMN id SET DEFAULT nextval('public.candidate_destination_id_seq'::regclass);


--
-- Name: candidate_education id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_education ALTER COLUMN id SET DEFAULT nextval('public.candidate_education_id_seq'::regclass);


--
-- Name: candidate_exam id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_exam ALTER COLUMN id SET DEFAULT nextval('public.candidate_exam_id_seq'::regclass);


--
-- Name: candidate_form id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_form ALTER COLUMN id SET DEFAULT nextval('public.candidate_form_id_seq'::regclass);


--
-- Name: candidate_job_experience id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_job_experience ALTER COLUMN id SET DEFAULT nextval('public.candidate_job_experience_id_seq'::regclass);


--
-- Name: candidate_language id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_language ALTER COLUMN id SET DEFAULT nextval('public.candidate_language_id_seq'::regclass);


--
-- Name: candidate_note id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_note ALTER COLUMN id SET DEFAULT nextval('public.candidate_note_id_seq'::regclass);


--
-- Name: candidate_occupation id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation ALTER COLUMN id SET DEFAULT nextval('public.candidate_occupation_id_seq'::regclass);


--
-- Name: candidate_opportunity id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_opportunity ALTER COLUMN id SET DEFAULT nextval('public.candidate_opportunity_id_seq'::regclass);


--
-- Name: candidate_property_definition id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_property_definition ALTER COLUMN id SET DEFAULT nextval('public.candidate_property_definition_id_seq'::regclass);


--
-- Name: candidate_review_item id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_review_item ALTER COLUMN id SET DEFAULT nextval('public.candidate_review_item_id_seq'::regclass);


--
-- Name: candidate_skill id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_skill ALTER COLUMN id SET DEFAULT nextval('public.candidate_skill_id_seq'::regclass);


--
-- Name: candidate_visa_check id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_check ALTER COLUMN id SET DEFAULT nextval('public.candidate_visa_check_id_seq'::regclass);


--
-- Name: candidate_visa_job_check id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_job_check ALTER COLUMN id SET DEFAULT nextval('public.candidate_visa_job_check_id_seq'::regclass);


--
-- Name: chat_post id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_post ALTER COLUMN id SET DEFAULT nextval('public.chat_post_id_seq'::regclass);


--
-- Name: country id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country ALTER COLUMN id SET DEFAULT nextval('public.country_id_seq'::regclass);


--
-- Name: duolingo_coupon id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_coupon ALTER COLUMN id SET DEFAULT nextval('public.duolingo_coupon_id_seq'::regclass);


--
-- Name: duolingo_extra_fields id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_extra_fields ALTER COLUMN id SET DEFAULT nextval('public.duolingo_extra_fields_id_seq'::regclass);


--
-- Name: education_level id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.education_level ALTER COLUMN id SET DEFAULT nextval('public.education_level_id_seq'::regclass);


--
-- Name: education_major id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.education_major ALTER COLUMN id SET DEFAULT nextval('public.education_major_id_seq'::regclass);


--
-- Name: employer id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employer ALTER COLUMN id SET DEFAULT nextval('public.employer_id_seq'::regclass);


--
-- Name: export_column id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.export_column ALTER COLUMN id SET DEFAULT nextval('public.export_column_id_seq'::regclass);


--
-- Name: help_link id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_link ALTER COLUMN id SET DEFAULT nextval('public.help_link_id_seq'::regclass);


--
-- Name: industry id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.industry ALTER COLUMN id SET DEFAULT nextval('public.industry_id_seq'::regclass);


--
-- Name: job_chat id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat ALTER COLUMN id SET DEFAULT nextval('public.job_chat_id_seq'::regclass);


--
-- Name: job_opp_intake id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_opp_intake ALTER COLUMN id SET DEFAULT nextval('public.job_opp_intake_id_seq'::regclass);


--
-- Name: language id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language ALTER COLUMN id SET DEFAULT nextval('public.language_id_seq'::regclass);


--
-- Name: language_level id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language_level ALTER COLUMN id SET DEFAULT nextval('public.language_level_id_seq'::regclass);


--
-- Name: link_preview id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.link_preview ALTER COLUMN id SET DEFAULT nextval('public.link_preview_id_seq'::regclass);


--
-- Name: nationality id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nationality ALTER COLUMN id SET DEFAULT nextval('public.nationality_id_seq'::regclass);


--
-- Name: occupation id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occupation ALTER COLUMN id SET DEFAULT nextval('public.occupation_id_seq'::regclass);


--
-- Name: offer_to_assist id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offer_to_assist ALTER COLUMN id SET DEFAULT nextval('public.offer_to_assist_id_seq'::regclass);


--
-- Name: partner id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner ALTER COLUMN id SET DEFAULT nextval('public.partner_id_seq'::regclass);


--
-- Name: reaction id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reaction ALTER COLUMN id SET DEFAULT nextval('public.reaction_id_seq'::regclass);


--
-- Name: root_request id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.root_request ALTER COLUMN id SET DEFAULT nextval('public.root_request_id_seq'::regclass);


--
-- Name: salesforce_job_opp id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp ALTER COLUMN id SET DEFAULT nextval('public.salesforce_job_opp_tc_job_id_seq'::regclass);


--
-- Name: saved_list id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list ALTER COLUMN id SET DEFAULT nextval('public.saved_list_id_seq'::regclass);


--
-- Name: saved_search id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search ALTER COLUMN id SET DEFAULT nextval('public.saved_search_id_seq'::regclass);


--
-- Name: search_join id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_join ALTER COLUMN id SET DEFAULT nextval('public.search_join_id_seq'::regclass);


--
-- Name: service_assignment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_assignment ALTER COLUMN id SET DEFAULT nextval('public.service_assignment_id_seq'::regclass);


--
-- Name: service_resource id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_resource ALTER COLUMN id SET DEFAULT nextval('public.service_resource_id_seq'::regclass);


--
-- Name: skills_tech_onet_en id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skills_tech_onet_en ALTER COLUMN id SET DEFAULT nextval('public.skills_tech_onet_en_id_seq'::regclass);


--
-- Name: survey_type id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.survey_type ALTER COLUMN id SET DEFAULT nextval('public.survey_type_id_seq'::regclass);


--
-- Name: system_language id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_language ALTER COLUMN id SET DEFAULT nextval('public.system_language_id_seq'::regclass);


--
-- Name: task id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task ALTER COLUMN id SET DEFAULT nextval('public.task_id_seq'::regclass);


--
-- Name: task_assignment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment ALTER COLUMN id SET DEFAULT nextval('public.task_assignment_id_seq'::regclass);


--
-- Name: translation id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.translation ALTER COLUMN id SET DEFAULT nextval('public.translation_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: audit_log audit_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_log
    ADD CONSTRAINT audit_log_pkey PRIMARY KEY (id);


--
-- Name: batch_job_execution_context batch_job_execution_context_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_execution_context
    ADD CONSTRAINT batch_job_execution_context_pkey PRIMARY KEY (job_execution_id);


--
-- Name: batch_job_execution batch_job_execution_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_execution
    ADD CONSTRAINT batch_job_execution_pkey PRIMARY KEY (job_execution_id);


--
-- Name: batch_job_instance batch_job_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_instance
    ADD CONSTRAINT batch_job_instance_pkey PRIMARY KEY (job_instance_id);


--
-- Name: batch_step_execution_context batch_step_execution_context_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_step_execution_context
    ADD CONSTRAINT batch_step_execution_context_pkey PRIMARY KEY (step_execution_id);


--
-- Name: batch_step_execution batch_step_execution_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_step_execution
    ADD CONSTRAINT batch_step_execution_pkey PRIMARY KEY (step_execution_id);


--
-- Name: candidate_certification candidate_certification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_certification
    ADD CONSTRAINT candidate_certification_pkey PRIMARY KEY (id);


--
-- Name: candidate_citizenship candidate_citizenship_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_citizenship
    ADD CONSTRAINT candidate_citizenship_pkey PRIMARY KEY (id);


--
-- Name: candidate_coupon_code candidate_coupon_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_coupon_code
    ADD CONSTRAINT candidate_coupon_code_pkey PRIMARY KEY (id);


--
-- Name: candidate_dependant candidate_dependant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_dependant
    ADD CONSTRAINT candidate_dependant_pkey PRIMARY KEY (id);


--
-- Name: candidate_destination candidate_destination_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_destination
    ADD CONSTRAINT candidate_destination_pkey PRIMARY KEY (id);


--
-- Name: candidate_education candidate_education_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_education
    ADD CONSTRAINT candidate_education_pkey PRIMARY KEY (id);


--
-- Name: candidate_exam candidate_exam_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_exam
    ADD CONSTRAINT candidate_exam_pkey PRIMARY KEY (id);


--
-- Name: candidate_attachment candidate_file_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_attachment
    ADD CONSTRAINT candidate_file_pkey PRIMARY KEY (id);


--
-- Name: candidate_form_instance candidate_form_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_form_instance
    ADD CONSTRAINT candidate_form_instance_pkey PRIMARY KEY (candidate_id, form_id);


--
-- Name: candidate_form candidate_form_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_form
    ADD CONSTRAINT candidate_form_pkey PRIMARY KEY (id);


--
-- Name: candidate_job_experience candidate_job_experience_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_job_experience
    ADD CONSTRAINT candidate_job_experience_pkey PRIMARY KEY (id);


--
-- Name: candidate_json_cache candidate_json_cache_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_json_cache
    ADD CONSTRAINT candidate_json_cache_pkey PRIMARY KEY (candidate_id);


--
-- Name: candidate_language candidate_language_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_pkey PRIMARY KEY (id);


--
-- Name: candidate_note candidate_note_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_note
    ADD CONSTRAINT candidate_note_pkey PRIMARY KEY (id);


--
-- Name: candidate_occupation candidate_occupation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_pkey PRIMARY KEY (id);


--
-- Name: candidate_opportunity candidate_opportunity_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_pkey PRIMARY KEY (id);


--
-- Name: candidate candidate_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_pkey PRIMARY KEY (id);


--
-- Name: candidate_property_definition candidate_property_definition_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_property_definition
    ADD CONSTRAINT candidate_property_definition_name_key UNIQUE (name);


--
-- Name: candidate_property_definition candidate_property_definition_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_property_definition
    ADD CONSTRAINT candidate_property_definition_pkey PRIMARY KEY (id);


--
-- Name: candidate_property candidate_property_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_property
    ADD CONSTRAINT candidate_property_pkey PRIMARY KEY (candidate_id, name);


--
-- Name: candidate_review_item candidate_review_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_pkey PRIMARY KEY (id);


--
-- Name: candidate_visa_job_check candidate_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_role_pkey PRIMARY KEY (id);


--
-- Name: candidate_saved_list candidate_saved_list_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_pkey PRIMARY KEY (candidate_id, saved_list_id);


--
-- Name: candidate_skill candidate_skill_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_skill
    ADD CONSTRAINT candidate_skill_pkey PRIMARY KEY (id);


--
-- Name: candidate_visa_check candidate_visa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_pkey PRIMARY KEY (id);


--
-- Name: chat_post chat_post_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_post
    ADD CONSTRAINT chat_post_pkey PRIMARY KEY (id);


--
-- Name: chatbot_message chatbot_message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chatbot_message
    ADD CONSTRAINT chatbot_message_pkey PRIMARY KEY (id);


--
-- Name: country_nationality_join country_nationality_join_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country_nationality_join
    ADD CONSTRAINT country_nationality_join_pkey PRIMARY KEY (country_id, nationality_id);


--
-- Name: country country_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country
    ADD CONSTRAINT country_pkey PRIMARY KEY (id);


--
-- Name: duolingo_coupon duolingo_coupon_coupon_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_coupon
    ADD CONSTRAINT duolingo_coupon_coupon_code_key UNIQUE (coupon_code);


--
-- Name: duolingo_coupon duolingo_coupon_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_coupon
    ADD CONSTRAINT duolingo_coupon_pkey PRIMARY KEY (id);


--
-- Name: duolingo_extra_fields duolingo_extra_fields_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_extra_fields
    ADD CONSTRAINT duolingo_extra_fields_pkey PRIMARY KEY (id);


--
-- Name: education_level education_level_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.education_level
    ADD CONSTRAINT education_level_pkey PRIMARY KEY (id);


--
-- Name: education_major education_major_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.education_major
    ADD CONSTRAINT education_major_pkey PRIMARY KEY (id);


--
-- Name: employer employer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employer
    ADD CONSTRAINT employer_pkey PRIMARY KEY (id);


--
-- Name: export_column export_column_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.export_column
    ADD CONSTRAINT export_column_pkey PRIMARY KEY (id);



--
-- Name: help_link help_link_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_link
    ADD CONSTRAINT help_link_pkey PRIMARY KEY (id);


--
-- Name: industry industry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.industry
    ADD CONSTRAINT industry_pkey PRIMARY KEY (id);


--
-- Name: job_chat job_chat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_pkey PRIMARY KEY (id);


--
-- Name: job_chat_user job_chat_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat_user
    ADD CONSTRAINT job_chat_user_pkey PRIMARY KEY (job_chat_id, user_id);


--
-- Name: batch_job_instance job_inst_un; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_instance
    ADD CONSTRAINT job_inst_un UNIQUE (job_name, job_key);


--
-- Name: job_opp_intake job_opp_intake_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_opp_intake
    ADD CONSTRAINT job_opp_intake_pkey PRIMARY KEY (id);


--
-- Name: job_suggested_saved_search job_suggested_saved_search_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_suggested_saved_search
    ADD CONSTRAINT job_suggested_saved_search_pkey PRIMARY KEY (tc_job_id, saved_search_id);


--
-- Name: language_level language_level_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language_level
    ADD CONSTRAINT language_level_pkey PRIMARY KEY (id);


--
-- Name: language language_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_pkey PRIMARY KEY (id);


--
-- Name: link_preview link_preview_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.link_preview
    ADD CONSTRAINT link_preview_pkey PRIMARY KEY (id);


--
-- Name: nationality nationality_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nationality
    ADD CONSTRAINT nationality_pkey PRIMARY KEY (id);


--
-- Name: occupation occupation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occupation
    ADD CONSTRAINT occupation_pkey PRIMARY KEY (id);


--
-- Name: offer_to_assist offer_to_assist_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offer_to_assist
    ADD CONSTRAINT offer_to_assist_pkey PRIMARY KEY (id);


--
-- Name: partner_job partner_job_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_job
    ADD CONSTRAINT partner_job_pkey PRIMARY KEY (partner_id, tc_job_id);


--
-- Name: partner partner_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner
    ADD CONSTRAINT partner_pkey PRIMARY KEY (id);


--
-- Name: partner_source_country partner_source_country_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_source_country
    ADD CONSTRAINT partner_source_country_pkey PRIMARY KEY (partner_id, country_id);


--
-- Name: reaction reaction_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reaction
    ADD CONSTRAINT reaction_pkey PRIMARY KEY (id);


--
-- Name: reaction_user reaction_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reaction_user
    ADD CONSTRAINT reaction_user_pkey PRIMARY KEY (reaction_id, user_id);


--
-- Name: root_request root_request_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.root_request
    ADD CONSTRAINT root_request_pkey PRIMARY KEY (id);


--
-- Name: salesforce_job_opp salesforce_job_opp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_pkey PRIMARY KEY (id);


--
-- Name: saved_list saved_list_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_pkey PRIMARY KEY (id);


--
-- Name: saved_search saved_search_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_pkey PRIMARY KEY (id);


--
-- Name: search_join search_join_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_join
    ADD CONSTRAINT search_join_pkey PRIMARY KEY (id);


--
-- Name: service_assignment service_assignment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_assignment
    ADD CONSTRAINT service_assignment_pkey PRIMARY KEY (id);


--
-- Name: service_resource service_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_resource
    ADD CONSTRAINT service_resource_pkey PRIMARY KEY (id);


--
-- Name: shedlock shedlock_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.shedlock
    ADD CONSTRAINT shedlock_pkey PRIMARY KEY (name);


--
-- Name: skills_esco_en skills_esco_en_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skills_esco_en
    ADD CONSTRAINT skills_esco_en_pk PRIMARY KEY (concepturi);


--
-- Name: skills_tech_onet_en skills_tech_onet_en_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skills_tech_onet_en
    ADD CONSTRAINT skills_tech_onet_en_pkey PRIMARY KEY (id);


--
-- Name: survey_type survey_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.survey_type
    ADD CONSTRAINT survey_type_pkey PRIMARY KEY (id);


--
-- Name: system_language system_language_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_language
    ADD CONSTRAINT system_language_pkey PRIMARY KEY (id);


--
-- Name: task_assignment task_assignment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_pkey PRIMARY KEY (id);


--
-- Name: task task_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT task_pkey PRIMARY KEY (id);


--
-- Name: task_saved_list task_saved_list_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_saved_list
    ADD CONSTRAINT task_saved_list_pkey PRIMARY KEY (saved_list_id, task_id);


--
-- Name: translation translation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.translation
    ADD CONSTRAINT translation_pkey PRIMARY KEY (id);


--
-- Name: candidate_form unique_name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_form
    ADD CONSTRAINT unique_name UNIQUE (name);


--
-- Name: candidate_occupation uq_candidate_occupation; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT uq_candidate_occupation UNIQUE (candidate_id, occupation_id);


--
-- Name: salesforce_job_opp uq_tc_job_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT uq_tc_job_id UNIQUE (id);


--
-- Name: candidate uq_user_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT uq_user_id UNIQUE (user_id);


--
-- Name: user_job user_job_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_job
    ADD CONSTRAINT user_job_pkey PRIMARY KEY (user_id, tc_job_id);


--
-- Name: user_saved_list user_saved_list_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_list
    ADD CONSTRAINT user_saved_list_pkey PRIMARY KEY (user_id, saved_list_id);


--
-- Name: user_saved_search user_saved_search_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_search
    ADD CONSTRAINT user_saved_search_pkey PRIMARY KEY (user_id, saved_search_id);


--
-- Name: user_source_country user_source_country_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_source_country
    ADD CONSTRAINT user_source_country_pkey PRIMARY KEY (user_id, country_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: candidate_candidate_number_uindex; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX candidate_candidate_number_uindex ON public.candidate USING btree (candidate_number);


--
-- Name: candidate_dependant_candidate_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_dependant_candidate_id_idx ON public.candidate_dependant USING btree (candidate_id);


--
-- Name: candidate_form_name_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_form_name_idx ON public.candidate_form USING btree (name);


--
-- Name: candidate_number_status_country_active_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_number_status_country_active_idx ON public.candidate USING btree (candidate_number, status, country_id) WHERE (status <> 'deleted'::text);


--
-- Name: candidate_public_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_public_id_idx ON public.candidate USING btree (public_id);


--
-- Name: candidate_registered_by_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_registered_by_idx ON public.candidate USING btree (registered_by);


--
-- Name: candidate_saved_list_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_saved_list_id_idx ON public.candidate_saved_list USING btree (saved_list_id);


--
-- Name: candidate_sflink_index; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_sflink_index ON public.candidate USING btree (sflink);


--
-- Name: candidate_status_index; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_status_index ON public.candidate USING btree (status);


--
-- Name: candidate_user_status_country_active_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_user_status_country_active_idx ON public.candidate USING btree (user_id, status, country_id) WHERE (status <> 'deleted'::text);


--
-- Name: candidate_visa_job_check_candidate_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_visa_job_check_candidate_id_idx ON public.candidate_visa_job_check USING btree (candidate_id);


--
-- Name: chatbot_message_question_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX chatbot_message_question_id_idx ON public.chatbot_message USING btree (question_id);


--
-- Name: chatbot_message_referenced_faq_ids_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX chatbot_message_referenced_faq_ids_idx ON public.chatbot_message USING gin (referenced_faq_ids);


--
-- Name: chatbot_message_session_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX chatbot_message_session_id_idx ON public.chatbot_message USING btree (session_id);


--
-- Name: country_iso_code_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX country_iso_code_idx ON public.country USING btree (iso_code);


--
-- Name: idx_candidate_external_id_country; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_candidate_external_id_country ON public.candidate USING btree (lower(external_id), country_id);


--
-- Name: idx_candidate_json_cache_version; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_candidate_json_cache_version ON public.candidate_json_cache USING btree (data_version);


--
-- Name: idx_candidate_phone_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_candidate_phone_lower ON public.candidate USING btree (lower(phone)) WHERE (status <> 'deleted'::text);


--
-- Name: idx_candidate_status_country; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_candidate_status_country ON public.candidate USING btree (status, country_id) WHERE (status <> 'deleted'::text);


--
-- Name: idx_candidate_whatsapp_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_candidate_whatsapp_lower ON public.candidate USING btree (lower(whatsapp)) WHERE (status <> 'deleted'::text);


--
-- Name: idx_users_email_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_email_lower ON public.users USING btree (lower(email));


--
-- Name: ip_address_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ip_address_idx ON public.root_request USING btree (ip_address);


--
-- Name: job_chat_candidate_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX job_chat_candidate_id_idx ON public.job_chat USING btree (candidate_id);


--
-- Name: job_chat_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX job_chat_id_idx ON public.chat_post USING btree (job_chat_id);


--
-- Name: job_chat_job_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX job_chat_job_id_idx ON public.job_chat USING btree (job_id);


--
-- Name: job_chat_source_partner_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX job_chat_source_partner_id_idx ON public.job_chat USING btree (source_partner_id);


--
-- Name: job_chat_type_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX job_chat_type_idx ON public.job_chat USING btree (type);


--
-- Name: language_iso_code_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX language_iso_code_idx ON public.language USING btree (iso_code);


--
-- Name: occupation_isco08_code_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX occupation_isco08_code_idx ON public.occupation USING btree (isco08_code);


--
-- Name: offer_to_assist_public_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX offer_to_assist_public_id_idx ON public.offer_to_assist USING btree (public_id);


--
-- Name: partner_public_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX partner_public_id_idx ON public.partner USING btree (public_id);


--
-- Name: sa_assigned_per_resource_uq_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX sa_assigned_per_resource_uq_idx ON public.service_assignment USING btree (resource_id) WHERE (((status)::text = 'ASSIGNED'::text) AND (resource_id IS NOT NULL));


--
-- Name: sa_candidate_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sa_candidate_idx ON public.service_assignment USING btree (candidate_id);


--
-- Name: sa_provider_service_candidate_status_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sa_provider_service_candidate_status_time_idx ON public.service_assignment USING btree (provider, service_code, candidate_id, status, assigned_at DESC);


--
-- Name: sa_resource_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sa_resource_idx ON public.service_assignment USING btree (resource_id);


--
-- Name: salesforce_job_opp_tc_job_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX salesforce_job_opp_tc_job_id_idx ON public.salesforce_job_opp USING btree (id);


--
-- Name: saved_list_public_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX saved_list_public_id_idx ON public.saved_list USING btree (public_id);


--
-- Name: saved_search_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX saved_search_id_idx ON public.user_saved_search USING btree (saved_search_id);


--
-- Name: saved_search_public_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX saved_search_public_id_idx ON public.saved_search USING btree (public_id);


--
-- Name: sr_provider_resource_uq_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX sr_provider_resource_uq_idx ON public.service_resource USING btree (provider, resource_code) WHERE (resource_code IS NOT NULL);


--
-- Name: sr_provider_sc_status_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sr_provider_sc_status_idx ON public.service_resource USING btree (provider, service_code, status);


--
-- Name: sr_provider_service_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sr_provider_service_idx ON public.service_resource USING btree (provider, service_code, id);


--
-- Name: task_name_uindex; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX task_name_uindex ON public.task USING btree (name);


--
-- Name: ts_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ts_idx ON public.candidate_job_experience USING gin (ts);


--
-- Name: ts_text_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ts_text_idx ON public.candidate USING gin (ts_text);


--
-- Name: user_job_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX user_job_id_idx ON public.user_job USING btree (tc_job_id);


--
-- Name: user_lower_username_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX user_lower_username_idx ON public.users USING btree (lower(username));


--
-- Name: user_saved_list_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX user_saved_list_id_idx ON public.user_saved_list USING btree (saved_list_id);


--
-- Name: users_full_name_rev_trgm_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX users_full_name_rev_trgm_idx ON public.users USING gin (lower(((TRIM(BOTH FROM last_name) || ' '::text) || TRIM(BOTH FROM first_name))) public.gin_trgm_ops);


--
-- Name: users_full_name_trgm_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX users_full_name_trgm_idx ON public.users USING gin (lower(((TRIM(BOTH FROM first_name) || ' '::text) || TRIM(BOTH FROM last_name))) public.gin_trgm_ops);


--
-- Name: users_lower_first_name_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX users_lower_first_name_idx ON public.users USING btree (lower(first_name)) WHERE (status <> 'deleted'::text);


--
-- Name: users_lower_last_name_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX users_lower_last_name_idx ON public.users USING btree (lower(last_name)) WHERE (status <> 'deleted'::text);


--
-- Name: users_partner_id_index; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX users_partner_id_index ON public.users USING btree (partner_id);


--
-- Name: users_username_active_status_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX users_username_active_status_idx ON public.users USING btree (lower(username)) WHERE (status <> 'deleted'::text);


--
-- Name: candidate_attachment candidate_attachment_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_attachment_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_attachment FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate candidate_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_bump_version BEFORE UPDATE ON public.candidate FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_self_version();


--
-- Name: candidate_certification candidate_certification_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_certification_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_certification FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_citizenship candidate_citizenship_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_citizenship_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_citizenship FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_coupon_code candidate_coupon_code_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_coupon_code_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_coupon_code FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_dependant candidate_dependant_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_dependant_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_dependant FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_destination candidate_destination_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_destination_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_destination FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_education candidate_education_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_education_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_education FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_exam candidate_exam_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_exam_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_exam FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_job_experience candidate_job_experience_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_job_experience_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_job_experience FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_language candidate_language_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_language_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_language FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_note candidate_note_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_note_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_note FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_occupation candidate_occupation_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_occupation_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_occupation FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_opportunity candidate_opportunity_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_opportunity_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_opportunity FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_property candidate_property_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_property_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_property FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_review_item candidate_review_item_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_review_item_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_review_item FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_skill candidate_skill_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_skill_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_skill FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_visa_check candidate_visa_check_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_visa_check_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_visa_check FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_visa_job_check candidate_visa_job_check_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_visa_job_check_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_visa_job_check FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: task_assignment task_assignment_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER task_assignment_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.task_assignment FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: users user_bump_candidate_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER user_bump_candidate_version AFTER INSERT OR DELETE OR UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_on_user_change();


--
-- Name: candidate_attachment candidate_attachment_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_attachment
    ADD CONSTRAINT candidate_attachment_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_birth_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_birth_country_id_fkey FOREIGN KEY (birth_country_id) REFERENCES public.country(id);


--
-- Name: candidate_certification candidate_certification_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_certification
    ADD CONSTRAINT candidate_certification_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_citizenship candidate_citizenship_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_citizenship
    ADD CONSTRAINT candidate_citizenship_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_citizenship candidate_citizenship_nationality_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_citizenship
    ADD CONSTRAINT candidate_citizenship_nationality_id_fkey FOREIGN KEY (nationality_id) REFERENCES public.country(id);


--
-- Name: candidate candidate_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate_coupon_code candidate_coupon_code_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_coupon_code
    ADD CONSTRAINT candidate_coupon_code_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_coupon_code candidate_coupon_code_offer_to_assist_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_coupon_code
    ADD CONSTRAINT candidate_coupon_code_offer_to_assist_id_fkey FOREIGN KEY (offer_to_assist_id) REFERENCES public.offer_to_assist(id);


--
-- Name: candidate candidate_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_dependant candidate_dependant_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_dependant
    ADD CONSTRAINT candidate_dependant_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_destination candidate_destination_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_destination
    ADD CONSTRAINT candidate_destination_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_destination candidate_destination_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_destination
    ADD CONSTRAINT candidate_destination_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate candidate_driving_license_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_driving_license_country_id_fkey FOREIGN KEY (driving_license_country_id) REFERENCES public.country(id);


--
-- Name: candidate_education candidate_education_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_education
    ADD CONSTRAINT candidate_education_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_education candidate_education_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_education
    ADD CONSTRAINT candidate_education_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate_education candidate_education_major_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_education
    ADD CONSTRAINT candidate_education_major_id_fkey FOREIGN KEY (major_id) REFERENCES public.education_major(id);


--
-- Name: candidate_exam candidate_exam_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_exam
    ADD CONSTRAINT candidate_exam_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_attachment candidate_file_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_attachment
    ADD CONSTRAINT candidate_file_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_attachment candidate_file_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_attachment
    ADD CONSTRAINT candidate_file_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_form_instance candidate_form_instance_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_form_instance
    ADD CONSTRAINT candidate_form_instance_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_form_instance candidate_form_instance_form_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_form_instance
    ADD CONSTRAINT candidate_form_instance_form_id_fkey FOREIGN KEY (form_id) REFERENCES public.candidate_form(id);


--
-- Name: candidate candidate_full_intake_completed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_full_intake_completed_by_fkey FOREIGN KEY (full_intake_completed_by) REFERENCES public.users(id);


--
-- Name: candidate_job_experience candidate_job_experience_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_job_experience
    ADD CONSTRAINT candidate_job_experience_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_job_experience candidate_job_experience_candidate_occupation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_job_experience
    ADD CONSTRAINT candidate_job_experience_candidate_occupation_id_fkey FOREIGN KEY (candidate_occupation_id) REFERENCES public.candidate_occupation(id);


--
-- Name: candidate_job_experience candidate_job_experience_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_job_experience
    ADD CONSTRAINT candidate_job_experience_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate_language candidate_language_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_language candidate_language_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_language_id_fkey FOREIGN KEY (language_id) REFERENCES public.language(id);


--
-- Name: candidate_language candidate_language_spoken_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_spoken_level_id_fkey FOREIGN KEY (spoken_level_id) REFERENCES public.language_level(id);


--
-- Name: candidate_language candidate_language_written_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_written_level_id_fkey FOREIGN KEY (written_level_id) REFERENCES public.language_level(id);


--
-- Name: candidate candidate_max_education_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_max_education_level_id_fkey FOREIGN KEY (max_education_level_id) REFERENCES public.education_level(id);


--
-- Name: candidate candidate_mini_intake_completed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_mini_intake_completed_by_fkey FOREIGN KEY (mini_intake_completed_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_nationality_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_nationality_id_fkey FOREIGN KEY (nationality_id) REFERENCES public.country(id);


--
-- Name: candidate candidate_nationalityold_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_nationalityold_id_fkey FOREIGN KEY (nationalityold_id) REFERENCES public.nationality(id);


--
-- Name: candidate_note candidate_note_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_note
    ADD CONSTRAINT candidate_note_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_note candidate_note_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_note
    ADD CONSTRAINT candidate_note_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_note candidate_note_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_note
    ADD CONSTRAINT candidate_note_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate_occupation candidate_occupation_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_occupation candidate_occupation_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_occupation candidate_occupation_occupation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_occupation_id_fkey FOREIGN KEY (occupation_id) REFERENCES public.occupation(id);


--
-- Name: candidate_occupation candidate_occupation_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate_opportunity candidate_opportunity_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_opportunity candidate_opportunity_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_opportunity candidate_opportunity_job_opp_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_job_opp_id_fkey FOREIGN KEY (job_opp_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: candidate_opportunity candidate_opportunity_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_partner_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_partner_candidate_id_fkey FOREIGN KEY (partner_candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate candidate_partner_edu_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_partner_edu_level_id_fkey FOREIGN KEY (partner_edu_level_id) REFERENCES public.education_level(id);


--
-- Name: candidate candidate_partner_english_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_partner_english_level_id_fkey FOREIGN KEY (partner_english_level_id) REFERENCES public.language_level(id);


--
-- Name: candidate candidate_partner_occupation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_partner_occupation_id_fkey FOREIGN KEY (partner_occupation_id) REFERENCES public.occupation(id);


--
-- Name: candidate_property candidate_property_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_property
    ADD CONSTRAINT candidate_property_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_property candidate_property_related_task_assignment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_property
    ADD CONSTRAINT candidate_property_related_task_assignment_id_fkey FOREIGN KEY (related_task_assignment_id) REFERENCES public.task_assignment(id);


--
-- Name: candidate candidate_registered_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_registered_by_fkey FOREIGN KEY (registered_by) REFERENCES public.partner(id);


--
-- Name: candidate candidate_relocated_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_relocated_country_id_fkey FOREIGN KEY (relocated_country_id) REFERENCES public.country(id);


--
-- Name: candidate_review_item candidate_review_item_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_review_item candidate_review_item_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_review_item candidate_review_item_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: candidate_review_item candidate_review_item_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate_visa_job_check candidate_role_occupation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_role_occupation_id_fkey FOREIGN KEY (occupation_id) REFERENCES public.occupation(id);


--
-- Name: candidate_saved_list candidate_saved_list_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_saved_list candidate_saved_list_saved_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_saved_list_id_fkey FOREIGN KEY (saved_list_id) REFERENCES public.saved_list(id);


--
-- Name: candidate_saved_list candidate_saved_list_shareable_cv_attachment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_shareable_cv_attachment_id_fkey FOREIGN KEY (shareable_cv_attachment_id) REFERENCES public.candidate_attachment(id);


--
-- Name: candidate_saved_list candidate_saved_list_shareable_doc_attachment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_shareable_doc_attachment_id_fkey FOREIGN KEY (shareable_doc_attachment_id) REFERENCES public.candidate_attachment(id);


--
-- Name: candidate candidate_shareable_cv_attachment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_shareable_cv_attachment_id_fkey FOREIGN KEY (shareable_cv_attachment_id) REFERENCES public.candidate_attachment(id);


--
-- Name: candidate candidate_shareable_doc_attachment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_shareable_doc_attachment_id_fkey FOREIGN KEY (shareable_doc_attachment_id) REFERENCES public.candidate_attachment(id);


--
-- Name: candidate_skill candidate_skill_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_skill
    ADD CONSTRAINT candidate_skill_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate candidate_survey_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_survey_type_id_fkey FOREIGN KEY (survey_type_id) REFERENCES public.survey_type(id);


--
-- Name: candidate candidate_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: candidate_visa_check candidate_visa_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_visa_check candidate_visa_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate_visa_check candidate_visa_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_visa_job_check candidate_visa_job_check_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_visa_job_check_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_visa_job_check candidate_visa_job_check_candidate_visa_check_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_visa_job_check_candidate_visa_check_id_fkey FOREIGN KEY (candidate_visa_check_id) REFERENCES public.candidate_visa_check(id);


--
-- Name: candidate_visa_job_check candidate_visa_job_check_job_opp_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_visa_job_check_job_opp_id_fkey FOREIGN KEY (job_opp_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: candidate_visa_check candidate_visa_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: chat_post chat_post_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_post
    ADD CONSTRAINT chat_post_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: chat_post chat_post_job_chat_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_post
    ADD CONSTRAINT chat_post_job_chat_id_fkey FOREIGN KEY (job_chat_id) REFERENCES public.job_chat(id);


--
-- Name: chat_post chat_post_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_post
    ADD CONSTRAINT chat_post_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: country_nationality_join country_nationality_join_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country_nationality_join
    ADD CONSTRAINT country_nationality_join_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: country_nationality_join country_nationality_join_nationality_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country_nationality_join
    ADD CONSTRAINT country_nationality_join_nationality_id_fkey FOREIGN KEY (nationality_id) REFERENCES public.nationality(id);


--
-- Name: duolingo_extra_fields duolingo_extra_fields_candidate_exam_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_extra_fields
    ADD CONSTRAINT duolingo_extra_fields_candidate_exam_id_fkey FOREIGN KEY (candidate_exam_id) REFERENCES public.candidate_exam(id);


--
-- Name: employer employer_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employer
    ADD CONSTRAINT employer_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: employer employer_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employer
    ADD CONSTRAINT employer_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: employer employer_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employer
    ADD CONSTRAINT employer_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: export_column export_column_saved_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.export_column
    ADD CONSTRAINT export_column_saved_list_id_fkey FOREIGN KEY (saved_list_id) REFERENCES public.saved_list(id);


--
-- Name: export_column export_column_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.export_column
    ADD CONSTRAINT export_column_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: duolingo_coupon fk_candidate; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_coupon
    ADD CONSTRAINT fk_candidate FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate fk_candidate_privacy_policy_partner; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT fk_candidate_privacy_policy_partner FOREIGN KEY (accepted_privacy_policy_partner_id) REFERENCES public.partner(id) ON DELETE SET NULL;


--
-- Name: help_link help_link_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_link
    ADD CONSTRAINT help_link_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: help_link help_link_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_link
    ADD CONSTRAINT help_link_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: help_link help_link_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_link
    ADD CONSTRAINT help_link_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: job_chat job_chat_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: job_chat job_chat_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: job_chat job_chat_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: job_chat job_chat_source_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_source_partner_id_fkey FOREIGN KEY (source_partner_id) REFERENCES public.partner(id);


--
-- Name: job_chat job_chat_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: job_chat_user job_chat_user_job_chat_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat_user
    ADD CONSTRAINT job_chat_user_job_chat_id_fkey FOREIGN KEY (job_chat_id) REFERENCES public.job_chat(id);


--
-- Name: job_chat_user job_chat_user_last_read_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat_user
    ADD CONSTRAINT job_chat_user_last_read_post_id_fkey FOREIGN KEY (last_read_post_id) REFERENCES public.chat_post(id);


--
-- Name: job_chat_user job_chat_user_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat_user
    ADD CONSTRAINT job_chat_user_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: batch_job_execution_context job_exec_ctx_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_execution_context
    ADD CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id) REFERENCES public.batch_job_execution(job_execution_id);


--
-- Name: batch_job_execution_params job_exec_params_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_execution_params
    ADD CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id) REFERENCES public.batch_job_execution(job_execution_id);


--
-- Name: batch_step_execution job_exec_step_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_step_execution
    ADD CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id) REFERENCES public.batch_job_execution(job_execution_id);


--
-- Name: batch_job_execution job_inst_exec_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_execution
    ADD CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id) REFERENCES public.batch_job_instance(job_instance_id);


--
-- Name: job_suggested_saved_search job_suggested_saved_search_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_suggested_saved_search
    ADD CONSTRAINT job_suggested_saved_search_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: job_suggested_saved_search job_suggested_saved_search_tc_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_suggested_saved_search
    ADD CONSTRAINT job_suggested_saved_search_tc_job_id_fkey FOREIGN KEY (tc_job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: link_preview link_preview_chat_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.link_preview
    ADD CONSTRAINT link_preview_chat_post_id_fkey FOREIGN KEY (chat_post_id) REFERENCES public.chat_post(id);


--
-- Name: offer_to_assist offer_to_assist_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offer_to_assist
    ADD CONSTRAINT offer_to_assist_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: offer_to_assist offer_to_assist_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offer_to_assist
    ADD CONSTRAINT offer_to_assist_partner_id_fkey FOREIGN KEY (partner_id) REFERENCES public.partner(id);


--
-- Name: offer_to_assist offer_to_assist_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offer_to_assist
    ADD CONSTRAINT offer_to_assist_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: partner partner_default_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner
    ADD CONSTRAINT partner_default_contact_id_fkey FOREIGN KEY (default_contact_id) REFERENCES public.users(id);


--
-- Name: partner partner_employer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner
    ADD CONSTRAINT partner_employer_id_fkey FOREIGN KEY (employer_id) REFERENCES public.employer(id);


--
-- Name: partner_job partner_job_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_job
    ADD CONSTRAINT partner_job_contact_id_fkey FOREIGN KEY (contact_id) REFERENCES public.users(id);


--
-- Name: partner_job partner_job_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_job
    ADD CONSTRAINT partner_job_partner_id_fkey FOREIGN KEY (partner_id) REFERENCES public.partner(id);


--
-- Name: partner_job partner_job_tc_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_job
    ADD CONSTRAINT partner_job_tc_job_id_fkey FOREIGN KEY (tc_job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: partner partner_redirect_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner
    ADD CONSTRAINT partner_redirect_partner_id_fkey FOREIGN KEY (redirect_partner_id) REFERENCES public.partner(id);


--
-- Name: partner_source_country partner_source_country_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_source_country
    ADD CONSTRAINT partner_source_country_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: partner_source_country partner_source_country_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_source_country
    ADD CONSTRAINT partner_source_country_partner_id_fkey FOREIGN KEY (partner_id) REFERENCES public.partner(id);


--
-- Name: reaction reaction_chat_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reaction
    ADD CONSTRAINT reaction_chat_post_id_fkey FOREIGN KEY (chat_post_id) REFERENCES public.chat_post(id);


--
-- Name: reaction_user reaction_user_reaction_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reaction_user
    ADD CONSTRAINT reaction_user_reaction_id_fkey FOREIGN KEY (reaction_id) REFERENCES public.reaction(id);


--
-- Name: reaction_user reaction_user_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reaction_user
    ADD CONSTRAINT reaction_user_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_contact_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_contact_user_id_fkey FOREIGN KEY (contact_user_id) REFERENCES public.users(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_country_object_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_country_object_id_fkey FOREIGN KEY (country_object_id) REFERENCES public.country(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_employer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_employer_id_fkey FOREIGN KEY (employer_id) REFERENCES public.employer(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_evergreen_child_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_evergreen_child_id_fkey FOREIGN KEY (evergreen_child_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_exclusion_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_exclusion_list_id_fkey FOREIGN KEY (exclusion_list_id) REFERENCES public.saved_list(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_job_opp_intake_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_job_opp_intake_id_fkey FOREIGN KEY (job_opp_intake_id) REFERENCES public.job_opp_intake(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_published_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_published_by_fkey FOREIGN KEY (published_by) REFERENCES public.users(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_recruiter_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_recruiter_partner_id_fkey FOREIGN KEY (recruiter_partner_id) REFERENCES public.partner(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_submission_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_submission_list_id_fkey FOREIGN KEY (submission_list_id) REFERENCES public.saved_list(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_suggested_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_suggested_list_id_fkey FOREIGN KEY (suggested_list_id) REFERENCES public.saved_list(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: saved_list saved_list_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: saved_list saved_list_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: saved_list saved_list_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: saved_list saved_list_saved_search_source_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_saved_search_source_id_fkey FOREIGN KEY (saved_search_source_id) REFERENCES public.saved_search(id);


--
-- Name: saved_list saved_list_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: saved_search saved_search_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: saved_search saved_search_default_save_selection_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_default_save_selection_list_id_fkey FOREIGN KEY (default_save_selection_list_id) REFERENCES public.saved_list(id);


--
-- Name: saved_search saved_search_exclusion_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_exclusion_list_id_fkey FOREIGN KEY (exclusion_list_id) REFERENCES public.saved_list(id);


--
-- Name: saved_search saved_search_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: saved_search saved_search_other_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_other_language_id_fkey FOREIGN KEY (other_language_id) REFERENCES public.language(id);


--
-- Name: saved_search saved_search_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: search_join search_join_child_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_join
    ADD CONSTRAINT search_join_child_search_id_fkey FOREIGN KEY (child_search_id) REFERENCES public.saved_search(id);


--
-- Name: search_join search_join_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_join
    ADD CONSTRAINT search_join_search_id_fkey FOREIGN KEY (search_id) REFERENCES public.saved_search(id);


--
-- Name: service_assignment service_assignment_actor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_assignment
    ADD CONSTRAINT service_assignment_actor_id_fkey FOREIGN KEY (actor_id) REFERENCES public.users(id);


--
-- Name: service_assignment service_assignment_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_assignment
    ADD CONSTRAINT service_assignment_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: service_assignment service_assignment_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_assignment
    ADD CONSTRAINT service_assignment_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES public.service_resource(id) ON DELETE SET NULL;


--
-- Name: batch_step_execution_context step_exec_ctx_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_step_execution_context
    ADD CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id) REFERENCES public.batch_step_execution(step_execution_id);


--
-- Name: system_language system_language_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_language
    ADD CONSTRAINT system_language_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: system_language system_language_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_language
    ADD CONSTRAINT system_language_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: task_assignment task_assignment_activated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_activated_by_fkey FOREIGN KEY (activated_by) REFERENCES public.users(id);


--
-- Name: task_assignment task_assignment_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: task_assignment task_assignment_deactivated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_deactivated_by_fkey FOREIGN KEY (deactivated_by) REFERENCES public.users(id);


--
-- Name: task_assignment task_assignment_related_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_related_list_id_fkey FOREIGN KEY (related_list_id) REFERENCES public.saved_list(id);


--
-- Name: task_assignment task_assignment_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.task(id);


--
-- Name: task task_candidate_form_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT task_candidate_form_id_fkey FOREIGN KEY (candidate_form_id) REFERENCES public.candidate_form(id);


--
-- Name: task task_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT task_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: task_saved_list task_saved_list_saved_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_saved_list
    ADD CONSTRAINT task_saved_list_saved_list_id_fkey FOREIGN KEY (saved_list_id) REFERENCES public.saved_list(id);


--
-- Name: task_saved_list task_saved_list_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_saved_list
    ADD CONSTRAINT task_saved_list_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.task(id);


--
-- Name: task task_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT task_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: translation translation_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.translation
    ADD CONSTRAINT translation_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: translation translation_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.translation
    ADD CONSTRAINT translation_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: user_job user_job_tc_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_job
    ADD CONSTRAINT user_job_tc_job_id_fkey FOREIGN KEY (tc_job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: user_job user_job_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_job
    ADD CONSTRAINT user_job_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_saved_list user_saved_list_saved_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_list
    ADD CONSTRAINT user_saved_list_saved_list_id_fkey FOREIGN KEY (saved_list_id) REFERENCES public.saved_list(id);


--
-- Name: user_saved_list user_saved_list_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_list
    ADD CONSTRAINT user_saved_list_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_saved_search user_saved_search_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_search
    ADD CONSTRAINT user_saved_search_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: user_saved_search user_saved_search_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_search
    ADD CONSTRAINT user_saved_search_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_source_country user_source_country_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_source_country
    ADD CONSTRAINT user_source_country_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: user_source_country user_source_country_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_source_country
    ADD CONSTRAINT user_source_country_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: users users_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: users users_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_partner_id_fkey FOREIGN KEY (partner_id) REFERENCES public.partner(id);


--
-- Name: users users_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

