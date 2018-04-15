import axios from 'axios';
import querystring from 'querystring';

export default class NowPlayingService {

    /**
     * Gets the available activity plugins
     * @returns {AxiosPromise}
     */
    getAvailableActivityPlugins() {
        return axios.get(API.ACTIVITIES.GET_AVAILABLE_PLUGINS);
    }


    /**
     * Gets the currently selected plugin
     * @returns {AxiosPromise}
     */
    getCurrentActivityPlugin() {
        return axios.get(API.ACTIVITIES.GET_CURRENT_PLUGIN);
    }

    /**
     * Saves the new activity plugin
     * @param pluginId
     * @returns {AxiosPromise}
     */
    setActivityPlugin(pluginId) {
        return axios.post(API.ACTIVITIES.SET_ACTIVITY_PLUGIN, querystring.stringify({plugin: pluginId}));
    }

    /**
     * GEts the list of activities for the currently selected activity plugin
      * @returns {AxiosPromise}
     */
    getActivities(){
        return axios.get(API.ACTIVITIES.GET_ACTIVITIES);
    }

    /**
     * gets the mapping activity / nowplaying plugin
     * @returns {AxiosPromise}
     */
    getActivityMapping(){
        return axios.get(API.ACTIVITIES.GET_MAPPING);
    }

    setMapping(activity, plugin){
        return axios.post(API.ACTIVITIES.SET_MAPPING, querystring.stringify({activity: activity, pluginId: plugin}))
    }

    /**
     * Gets the available now playing plugins
     * @returns {AxiosPromise}
     */
    getNowPlayingPlugins(){
        return axios.get(API.NOW_PLAYING.GET_AVAILABLE_PLUGINS);
    }

    /**
     * Saves the settings for a single plugin
     * @param settings
     * @returns {AxiosPromise}
     */
    saveSetting(settings){
        return axios.post(API.SETTINGS.SAVE, querystring.stringify(settings));
    }

}