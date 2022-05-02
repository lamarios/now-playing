import querystring from 'querystring';

export const API_URL = "/api";
export const API = {
    ACTIVITIES: {
        GET_AVAILABLE_PLUGINS: API_URL + "/activities/get-available-plugins",
        SET_ACTIVITY_PLUGIN: API_URL + "/activities/set",
        GET_ACTIVITIES: API_URL + "/activities/get-activities"
    },
    NOW_PLAYING: {
        GET_AVAILABLE_PLUGINS: API_URL + "/now-playing/get-available-plugins",
        GET_IMAGE: "/now-playing.jpg?width={0}&height={1}&scale={2}"
    },
    FLOW: {
        SAVE_FLOW: API_URL + "/flow",
        GET_FLOW: API_URL + "/flow"
    },
    SETTINGS: {
        SAVE: API_URL + "/settings/save",
    }
};

class NowPlayingService {

    /**
     * Gets the available activity plugins
     * @returns {Promise}
     */
    getAvailableActivityPlugins() {
        return fetch(API.ACTIVITIES.GET_AVAILABLE_PLUGINS).then(r => r.json());
    }



    /**
     * Saves the new activity plugin
     * @param pluginId
     * @returns {Promise}
     */
    setActivityPlugin(pluginId) {
        return fetch(API.ACTIVITIES.SET_ACTIVITY_PLUGIN, {
            method: 'POST',
            body: new URLSearchParams({plugin: pluginId})
        }).then(r => r.json());
    }

    /**
     * GEts the list of activities for a given activity plugin
     * @returns {Promise}
     */
    getActivitiesForPlugin(pluginId) {
        return fetch(API.ACTIVITIES.GET_ACTIVITIES + "/" + pluginId).then(r => r.json());
    }


    setMapping(activity, plugin) {
        return fetch(API.ACTIVITIES.SET_MAPPING, {
            method: 'POST', body:
                new URLSearchParams({
                    activity: activity,
                    pluginId: plugin
                })
        }).then(r => r.json())
    }

    /**
     * Gets the available now playing plugins
     * @returns {Promise}
     */
    getNowPlayingPlugins() {
        return fetch(API.NOW_PLAYING.GET_AVAILABLE_PLUGINS).then(r => r.json());
    }

    /**
     * Saves the settings for a single plugin
     * @param settings
     * @returns {Promise}
     */
    saveSetting(settings) {

        return fetch(API.SETTINGS.SAVE, {method: 'post', body: new URLSearchParams(settings)}).then(r => r.json());
    }


    /**
     *  saves the flow of now playing
     */
    saveFlow(flow) {
        return fetch(API.FLOW.SAVE_FLOW, {
            method: 'post',
            body: JSON.stringify(flow),
            headers: {
                'Content-Type': 'application/json'
            }
        })
    }

    /**
     * GEts the current flow
     * @returns {Promise<any>}
     */
    getFlow(){
        return fetch(API.FLOW.GET_FLOW).then(r => r.json());
    }

}

export const Service = new NowPlayingService();