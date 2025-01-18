package com.hackathon.deployer.dto;

import net.sf.json.JSONObject;

public class ResponseDto {
    public String tableName;
    public JSONObject tableSchema;
    public String insertEndpoint;
    public String fetchEndpoint;
}
