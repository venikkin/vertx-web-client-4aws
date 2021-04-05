package main

import (
	"encoding/json"
	"fmt"
	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
	"net/url"
	"strings"
)

func main() {
	lambda.Start(handle)
}

func handle(request events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	fmt.Printf("Received %+v/n", request)
	nicelyParsedBody, err := parseBodyNicely(request)
	if err != nil {
		return events.APIGatewayProxyResponse{}, err
	}
	echo := echo{
		Method:                    request.HTTPMethod,
		QueryParameters:           request.QueryStringParameters,
		MultiValueQueryParameters: request.MultiValueQueryStringParameters,
		Body:                      nicelyParsedBody,
		Headers:                   request.Headers,
	}
	echoBytes, err := json.Marshal(&echo)
	if err != nil {
		return events.APIGatewayProxyResponse{}, err
	}
	return events.APIGatewayProxyResponse{
		StatusCode: 200,
		Body:       string(echoBytes),
	}, nil
}

func parseBodyNicely(request events.APIGatewayProxyRequest) (interface{}, error) {
	if request.Body == "" {
		return "", nil
	}
	contentType := findContentType(request.Headers)
	if strings.Contains(contentType, "application/json") {
		var jsonObj interface{}
		err := json.Unmarshal([]byte(request.Body), &jsonObj)
		return jsonObj, err
	}
	if strings.Contains(contentType, "application/x-www-form-urlencoded") {
		formParameters := make(map[string]interface{}, 0)
		pairs := strings.Split(request.Body, "&")
		for _, pair := range pairs {
			keyValue := strings.Split(pair, "=")
			key := urlDecode(keyValue[0])
			value := urlDecode(keyValue[1])

			previousValues, found := formParameters[key]
			if !found {
				formParameters[key] = value
			} else {
				stringValue, isString := previousValues.(string)
				if isString {
					formParameters[key] = []string{stringValue, value}
				} else {
					arrayValue, isArray := previousValues.([]string)
					if isArray {
						formParameters[key] = append(arrayValue, value)
					}
				}
			}
		}
		return formParameters, nil
	}
	return request.Body, nil
}

func urlDecode(s string) string {
	trimmed := strings.TrimSpace(s)
	decoded, err := url.QueryUnescape(trimmed)
	if err != nil {
		fmt.Printf("Fail to decode %s. Error: %+v\n", s, err)
		return trimmed
	}
	return decoded
}

func findContentType(headers map[string]string) string {
	for headerKey, headerValue := range headers {
		if strings.ToLower(headerKey) == "content-type" {
			return strings.ToLower(headerValue)
		}
	}
	return ""
}

type echo struct {
	Method                    string              `json:"method"`
	QueryParameters           map[string]string   `json:"queryParameters"`
	MultiValueQueryParameters map[string][]string `json:"multiValueQueryParameters"`
	Body                      interface{}         `json:"body"`
	Headers                   map[string]string   `json:"headers"`
}
