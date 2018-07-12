import {
	NativeModules,
	AppState,
	Platform
 } from 'react-native';

const { OfflineWeb } = NativeModules;

class OfflineWebModule {
	constructor(url) {}

	startWebModule() {
        return OfflineWebModule.start(this.url);
	}

	get origin() {
		return this._origin;
	}
}

export default StaticServer;