package com.springml.slack.command;

import static com.springml.slack.util.Constants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.springml.salesforce.wave.api.ForceAPI;
import com.springml.salesforce.wave.impl.ForceAPIImpl;
import com.springml.salesforce.wave.model.SOQLResult;
import com.springml.salesforce.wave.util.SFConfig;
import com.springml.slack.command.cache.OpportunityListCache;
import com.springml.slack.command.model.Command;
import com.springml.slack.resource.SlackResource;

public class OpportunitiesCommand implements CommandExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(OpportunitiesCommand.class);

    @Value("${sf.username}")
    private String sfUsername;
    @Value("${sf.password}")
    private String sfPassword;
    @Value("${sf.login.url}")
    private String sfLoginURL;
    @Value("${sf.api.version}")
    private String sfAPIVersion;
    @Autowired
    private OpportunityListCache listCache;
    @Autowired
    private SlackResource slackResource;

    @PostConstruct
    public void init() {
        slackResource.registerExecutor("/opportunities", this);
    }

    // TODO - Add Cache for this method as well
    public String execute(Command command) throws Exception {
        LOG.info("Command to be executed " + command);
        String text = command.getText();
        String soql = getSOQLQuery(text);
        ForceAPI forceAPI = getForceAPI(command.getTeam_id());
        SOQLResult result = forceAPI.query(soql);
        List<Map<String, String>> records = result.filterRecords();

        StringBuilder response = new StringBuilder();
        if (records.isEmpty()) {
            LOG.info("No results found for text " + command.getText());
            response.append("None of the opportunites matches your search criteria");
        } else {
            response.append("Below opportunities matches your search criteria");
            int count = 1;
            List<String> cacheEntry = new ArrayList<>();
            for (Map<String, String> record : records) {
                String opprId = record.get("Id");
                String opprName = record.get("Name");
                response.append(STR_NEWLINE).append(count++).append(STR_PERIOD).append(STR_SPACE);
                response.append(opprName).append(STR_SPACE);
                response.append(STR_OPEN_BRACKET).append(opprId).append(STR_CLOSE_BRACKET);

                cacheEntry.add(opprId);
            }

            listCache.add(command.getTeam_id(), cacheEntry);
            LOG.info("Found " + count + " results for text " + command.getText());
        }

        return response.toString();
    }

    private String getSOQLQuery(String text) {
        StringBuilder soql = new StringBuilder();
        soql.append("select Id, Name, Account.Name, Owner.Name from Opportunity where Opportunity.Account.Name in (");
        String[] args = text.split(STR_SPACE);

        StringBuilder valueClause = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                valueClause.append(STR_COMMA);
            }
            valueClause.append(STR_QUOTE).append(args[i]).append(STR_QUOTE);
        }

        soql.append(valueClause);
        soql.append(") or Opportunity.Owner.Name in (");
        soql.append(valueClause);
        soql.append(")");

        return soql.toString();
    }

    private ForceAPI getForceAPI(String teamId) throws Exception {
        // TODO : Need to get corresponding salesforce Id using the teamId
        // Currently it is configured via application.properties

        SFConfig sfConfig = new SFConfig(sfUsername, sfPassword, sfLoginURL, sfAPIVersion);
        return new ForceAPIImpl(sfConfig);
    }

}
