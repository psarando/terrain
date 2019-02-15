(ns terrain.routes.comments
  (:use [common-swagger-api.schema]
        [ring.util.http-response :only [ok]]
        [terrain.routes.schemas.comments]
        [terrain.routes.schemas.filesystem])
  (:require [common-swagger-api.schema.apps :as app-schema]
            [common-swagger-api.schema.metadata.comments :as comment-schema]
            [terrain.services.metadata.comments :as comments]
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
        :return comment-schema/CommentList
        :description "Lists all of the comments associated with a file or foler in the data store."
        (ok (comments/list-data-comments entry-id)))

      (POST "/" []
        :summary "Add a File or Folder Comment"
        :body [body (describe comment-schema/CommentRequest "The comment to add")]
        :return comment-schema/CommentResponse
        :description "Adds a comment to the file or folder with the given ID."
        (ok (comments/add-data-comment entry-id body)))

      (PATCH "/:comment-id" []
        :summary "Retract or Readmit a File or Folder Comment"
        :path-params [comment-id :- comment-schema/CommentIdPathParam]
        :query [{:keys [retracted]} RetractCommentQueryParams]
        :description-file "docs/patch-data-entry-comment.md"
        (comments/update-data-retract-status entry-id comment-id retracted)
        (ok)))))

(defn admin-data-comment-routes
  []
  (util/optional-routes
    [#(and (config/filesystem-routes-enabled) (config/metadata-routes-enabled))]

    (context "/filsystem/entry/:entry-id/comments" []
      :path-params [entry-id :- DataIdPathParam]
      :tags ["admin-filesystem"]

      (DELETE "/:comment-id" []
        :summary "Delete a File or Folder Comment"
        :path-params [comment-id :- comment-schema/CommentIdPathParam]
        :description "Allows an administrator to delete a file or folder comment."
        (comments/delete-data-comment entry-id comment-id)
        (ok)))))

(defn app-comment-routes
  []
  (util/optional-routes
    [#(and (config/app-routes-enabled) (config/metadata-routes-enabled))]

    (context "/apps/:app-id/comments" []
      :path-params [app-id :- app-schema/AppIdParam]
      :tags ["apps"]

      (GET "/" []
        :summary "Get App Comments"
        :return comment-schema/CommentList
        :description "Lists all of the comments associated with an app."
        (ok (comments/list-app-comments app-id)))

      (POST "/" []
        :summary "Add an App Comment"
        :body [body (describe comment-schema/CommentRequest "The comment to add")]
        :return comment-schema/CommentResponse
        :description "Adds a comment to the app with the given ID."
        (ok (comments/add-app-comment app-id body)))

      (PATCH "/:comment-id" [app-id comment-id retracted]
        (comments/update-app-retract-status app-id comment-id retracted)))))

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
