(ns terrain.routes.comments
  (:use [common-swagger-api.schema]
        [common-swagger-api.schema.metadata.comments]
        [ring.util.http-response :only [ok]]
        [terrain.routes.schemas.filesystem])
  (:require [terrain.services.metadata.comments :as comments]
            [terrain.util :as util]
            [terrain.util.config :as config]))

(defn data-comment-routes
  []
  (util/optional-routes
    [#(and (config/filesystem-routes-enabled) (config/metadata-routes-enabled))]

    (context "/filesystem/entry/:entry-id/comments" []
      :path-params [entry-id :- DataIdPathParam]
      :tags ["filesystem"]

      (GET "/" []
        :summary "Get File or Folder Comments"
        :return CommentList
        :description "Lists all of the comments associated with a file or foler in the data store."
        (ok (comments/list-data-comments entry-id)))

      (POST "/" [entry-id :as {body :body}]
        (comments/add-data-comment entry-id body))

      (PATCH "/:comment-id" [entry-id comment-id retracted]
        (comments/update-data-retract-status entry-id comment-id retracted)))))

(defn admin-data-comment-routes
  []
  (util/optional-routes
    [#(and (config/filesystem-routes-enabled) (config/metadata-routes-enabled))]

    (DELETE "/filesystem/entry/:entry-id/comments/:comment-id"
      [entry-id comment-id]
      (comments/delete-data-comment entry-id comment-id))))

(defn app-comment-routes
  []
  (util/optional-routes
    [#(and (config/app-routes-enabled) (config/metadata-routes-enabled))]

    (GET "/apps/:app-id/comments" [app-id]
      (comments/list-app-comments app-id))

    (POST "/apps/:app-id/comments" [app-id :as {body :body}]
      (comments/add-app-comment app-id body))

    (PATCH "/apps/:app-id/comments/:comment-id"
      [app-id comment-id retracted]
      (comments/update-app-retract-status app-id comment-id retracted))))

(defn admin-app-comment-routes
  []
  (util/optional-routes
    [#(and (config/app-routes-enabled) (config/metadata-routes-enabled))]

    (DELETE "/apps/:app-id/comments/:comment-id"
      [app-id comment-id]
      (comments/delete-app-comment app-id comment-id))))

(defn admin-comment-routes
  []
  (util/optional-routes
   [#(and (config/app-routes-enabled) (config/metadata-routes-enabled))]

   (GET "/comments/:commenter-id" [commenter-id]
     (comments/list-comments-by-user commenter-id))

   (DELETE "/comments/:commenter-id" [commenter-id]
     (comments/delete-comments-by-user commenter-id))))
