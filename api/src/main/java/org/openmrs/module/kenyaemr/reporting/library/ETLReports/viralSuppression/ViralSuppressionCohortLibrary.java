/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemr.reporting.library.ETLReports.viralSuppression;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Library of cohort definitions for viral suppression
 */
@Component
public class ViralSuppressionCohortLibrary {
    public CohortDefinition suppressed(){
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery = "select t.patient_id\n" +
                "from(\n" +
                "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n" +
                "           greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n" +
                "           greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n" +
                "           greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n" +
                "           d.patient_id as disc_patient,\n" +
                "           d.effective_disc_date as effective_disc_date,\n" +
                "           max(d.visit_date) as date_discontinued,\n" +
                "           de.patient_id as started_on_drugs,\n" +
                "           mid(max(concat(vl_result.visit_date, vl_result.vl_result)), 11) vl_result\n" +
                "            from kenyaemr_etl.etl_patient_hiv_followup fup\n" +
                "           join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n" +
                "           join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n" +
                "           left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n" +
                "           left outer JOIN\n" +
                "             (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n" +
                "              where date(visit_date) <= date(:endDate) and program_name='HIV'\n" +
                "              group by patient_id\n" +
                "             ) d on d.patient_id = fup.patient_id\n" +
                "           left outer join\n" +
                "             (\n" +
                "             select\n" +
                "                    patient_id,\n" +
                "                    visit_date,\n" +
                "                    if(lab_test = 856, test_result, if(lab_test=1305 and test_result = 1302, \"LDL\", \"\")) as vl_result\n" +
                "             from kenyaemr_etl.etl_laboratory_extract\n" +
                "             where lab_test in (1305, 856)\n" +
                "             ) vl_result on vl_result.patient_id = fup.patient_id\n" +
                "    where fup.visit_date <= date(:endDate)\n" +
                "    group by patient_id\n" +
                "    having (started_on_drugs is not null and started_on_drugs <> '') and (vl_result=\"LDL\" or vl_result < 1000) and (\n" +
                "        (\n" +
                "            ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and (date(d.effective_disc_date) > date(:endDate) or d.effective_disc_date is null))\n" +
                "              and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null)\n" +
                "            )\n" +
                "        )\n" +
                "    )t;";
        cd.setName("suppressed");
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setQuery(sqlQuery);
        cd.setDescription("Suppressed");

        return cd;
    }

    public  CohortDefinition unsuppressed() {
        SqlCohortDefinition cd = new SqlCohortDefinition();
        String sqlQuery="select t.patient_id\n" +
                "from(\n" +
                "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n" +
                "           greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n" +
                "           greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n" +
                "           greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n" +
                "           d.patient_id as disc_patient,\n" +
                "           d.effective_disc_date as effective_disc_date,\n" +
                "           max(d.visit_date) as date_discontinued,\n" +
                "           de.patient_id as started_on_drugs,\n" +
                "           mid(max(concat(vl_result.visit_date, vl_result.vl_result)), 11) vl_result\n" +
                "    from kenyaemr_etl.etl_patient_hiv_followup fup\n" +
                "           join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n" +
                "           join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n" +
                "           left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n" +
                "           left outer JOIN\n" +
                "             (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n" +
                "              where date(visit_date) <= date(:endDate) and program_name='HIV'\n" +
                "              group by patient_id\n" +
                "             ) d on d.patient_id = fup.patient_id\n" +
                "           left outer join\n" +
                "             (\n" +
                "             select\n" +
                "                    patient_id,\n" +
                "                    visit_date,\n" +
                "                    if(lab_test = 856, test_result, if(lab_test=1305 and test_result = 1302, \"LDL\", \"\")) as vl_result\n" +
                "             from kenyaemr_etl.etl_laboratory_extract\n" +
                "             where lab_test in (1305, 856)\n" +
                "             ) vl_result on vl_result.patient_id = fup.patient_id\n" +
                "    where fup.visit_date <= date(:endDate)\n" +
                "    group by patient_id\n" +
                "    having (started_on_drugs is not null and started_on_drugs <> '' and  vl_result >= 1000) and (\n" +
                "        (\n" +
                "            ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and (date(d.effective_disc_date) > date(:endDate) or d.effective_disc_date is null))\n" +
                "              and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null)\n" +
                "            )\n" +
                "        )\n" +
                "    )t;" ;


        cd.setName("unsuppressed");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("Unsuppressed");

        return cd;
    }

    public  CohortDefinition noVLResults() {
        String sqlQuery="select t.patient_id\n" +
                "from(\n" +
                "    select fup.visit_date,fup.patient_id, max(e.visit_date) as enroll_date,\n" +
                "           greatest(max(e.visit_date), ifnull(max(date(e.transfer_in_date)),'0000-00-00')) as latest_enrolment_date,\n" +
                "           greatest(max(fup.visit_date), ifnull(max(d.visit_date),'0000-00-00')) as latest_vis_date,\n" +
                "           greatest(mid(max(concat(fup.visit_date,fup.next_appointment_date)),11), ifnull(max(d.visit_date),'0000-00-00')) as latest_tca,\n" +
                "           d.patient_id as disc_patient,\n" +
                "           d.effective_disc_date as effective_disc_date,\n" +
                "           max(d.visit_date) as date_discontinued,\n" +
                "           de.patient_id as started_on_drugs,\n" +
                "           mid(max(concat(vl_result.visit_date, vl_result.vl_result)), 11) vl_result\n" +
                "             from kenyaemr_etl.etl_patient_hiv_followup fup\n" +
                "           join kenyaemr_etl.etl_patient_demographics p on p.patient_id=fup.patient_id\n" +
                "           join kenyaemr_etl.etl_hiv_enrollment e on fup.patient_id=e.patient_id\n" +
                "           left outer join kenyaemr_etl.etl_drug_event de on e.patient_id = de.patient_id and de.program='HIV' and date(date_started) <= date(:endDate)\n" +
                "           left outer JOIN\n" +
                "             (select patient_id, coalesce(date(effective_discontinuation_date),visit_date) visit_date,max(date(effective_discontinuation_date)) as effective_disc_date from kenyaemr_etl.etl_patient_program_discontinuation\n" +
                "              where date(visit_date) <= date(:endDate) and program_name='HIV'\n" +
                "              group by patient_id\n" +
                "             ) d on d.patient_id = fup.patient_id\n" +
                "           left outer join\n" +
                "             (\n" +
                "             select\n" +
                "                    patient_id,\n" +
                "                    visit_date,\n" +
                "                    if(lab_test = 856, test_result, if(lab_test=1305 and test_result = 1302, \"LDL\", \"\")) as vl_result\n" +
                "             from kenyaemr_etl.etl_laboratory_extract\n" +
                "             where lab_test in (1305, 856)\n" +
                "             ) vl_result on vl_result.patient_id = fup.patient_id\n" +
                "    where fup.visit_date <= date(:endDate)\n" +
                "    group by patient_id\n" +
                "    having (started_on_drugs is not null and started_on_drugs <> '' and (vl_result is null or vl_result = '')) and (\n" +
                "        (\n" +
                "            ((timestampdiff(DAY,date(latest_tca),date(:endDate)) <= 30 or timestampdiff(DAY,date(latest_tca),date(curdate())) <= 30) and (date(d.effective_disc_date) > date(:endDate) or d.effective_disc_date is null))\n" +
                "              and (date(latest_vis_date) >= date(date_discontinued) or date(latest_tca) >= date(date_discontinued) or disc_patient is null)\n" +
                "            )\n" +
                "        )\n" +
                "    )t;";
        SqlCohortDefinition cd = new SqlCohortDefinition();
        cd.setName("noVLResults");
        cd.setQuery(sqlQuery);
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.setDescription("No VL Results");
        return cd;
    }

}
