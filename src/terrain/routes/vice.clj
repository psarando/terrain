(ns terrain.routes.vice
  (:use [common-swagger-api.schema]
        [ring.util.http-response :only [ok]]
        [terrain.auth.user-attributes :only [current-user]]
        [terrain.services.user-info]
        [terrain.util])
  (:require [terrain.clients.app-exposer :as vice]
            [terrain.routes.schemas.vice :as vice-schema]
            [terrain.util.config :as config]))

(defn admin-vice-routes
  []
  (optional-routes
    [config/app-routes-enabled]
    
    (context "/vice" []
      :tags ["admin-vice"]
      
      (GET "/resources" []
        :query [filter vice-schema/FilterParams]
        :return vice-schema/FullResourceListing
        :summary "List Kubernetes resources deployed in the cluster"
        :description "Lists all Kubernetes resources associated with an analysis running in the cluster."
        (ok (vice/get-resources filter)))

      (GET "/async-data" []
        :query [params vice-schema/AsyncDataParams]
        :return vice-schema/AsyncData
        :summary "Get data that is generated asynchronously"
        :description "Get data for the VICE analysis that is generated asynchronously"
        (ok (vice/async-data params)))
      
      (DELETE "/analyses/:external-id" []
        :path-params [external-id :- vice-schema/ExternalID]
        :summary "Cancel VICE analysis, send outputs to data store"
        :description "Cancels the VICE analysis after triggering the transfers of the output to the data store and waiting for them to complete"
        (ok (vice/cancel-analysis external-id)))
      
      (GET "/analyses/:analysis-id/time-limit" []
        :path-params [analysis-id :- vice-schema/AnalysisID]
        :query [params vice-schema/TimeLimitQueryParams]
        :return vice-schema/TimeLimit
        :summary "Get current time limit"
        :description "Gets the current time limit set for the analysis"
        (ok (vice/get-time-limit analysis-id (:user params))))
        
      (POST "/analyses/:analysis-id/time-limit" []
        :path-params [analysis-id :- vice-schema/AnalysisID]
        :query [params vice-schema/TimeLimitQueryParams]
        :return vice-schema/TimeLimit
        :summary "Extend the time limit"
        :description "Extends the time limit for the analysis by 3 days"
        (ok (vice/set-time-limit analysis-id (:user params)))))))
