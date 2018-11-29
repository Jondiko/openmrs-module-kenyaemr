package org.openmrs.module.kenyaemr.reporting.cohort.definition.evaluator.pmtct.maternity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.reporting.EmrReportingUtils;
import org.openmrs.module.kenyaemr.reporting.cohort.definition.pmtct.maternity.MaternityClientsWithAPHCohortDefinition;
import org.openmrs.module.kenyaemr.reporting.cohort.definition.pmtct.maternity.MaternityDeathsCohortDefinition;
import org.openmrs.module.kenyaemr.reporting.library.ETLReports.MOH731.ETLMoh731CohortLibrary;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Evaluator for maternity deaths
 */
@Handler(supports = {MaternityDeathsCohortDefinition.class})
public class MaternityClientsDeadCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
    @Autowired
    EvaluationService evaluationService;
    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {

        MaternityDeathsCohortDefinition definition = (MaternityDeathsCohortDefinition) cohortDefinition;
        if (definition == null)
            return null;

        String query = "select ld.patient_id from kenyaemr_etl.etl_mchs_delivery ld where ld.condition_of_mother=134612;";
        Cohort newCohort = new Cohort();
        SqlQueryBuilder builder = new SqlQueryBuilder();
        builder.append(query);
        Date startDate = (Date)context.getParameterValue("startDate");
        Date endDate = (Date)context.getParameterValue("endDate");
        builder.addParameter("endDate", endDate);
        builder.addParameter("startDate", startDate);
        List<Integer> ptIds = evaluationService.evaluateToList(builder, Integer.class, context);

        newCohort.setMemberIds(new HashSet<Integer>(ptIds));


        return new EvaluatedCohort(newCohort, definition, context);
    }

}