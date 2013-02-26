/**
 * Implements a view on the local installed bundles and JPM.
 */
(function() {

	/**
	 * Global scope. Referred to from the html so needs to be global
	 */
	window.AqConsolePluginCtl = function($scope, $location) {
		$scope.main = {}

		$scope.go = function(to) {
			location = '#/' + to;
		}
	}

	/**
	 * Local function wich is used as the main controller We retrieve 2 lists: a
	 * list of bundles and a list programs. The following functions manages the
	 * model of this.
	 */

	function AqSearch($scope, $location, $http, $routeParams) {

		/**
		 * Sends a request
		 */
		function send(action, location, bundleId) {
			$scope.main.alert = "Loading ...";
			var h = {
				action : action,
				query : $scope.query
			};
			if (location)
				h.location = location;

			if (bundleId)
				h.bundleId = bundleId;

			$http.post("plugins/", h).success(function(data) {
				$scope.bundles = data.bundles || $scope.bundles;
				$scope.programs = data.programs || $scope.programs;
				$scope.programs = data.programs || $scope.programs;
				$scope.main.alert = data.error;
				if (!data.error && action === 'INSTALL')
					document.location.reload(true);

			}).error(function(data, status) {
				$scope.main.alert = 'Error ' + status;
			})
		}

		/**
		 * Finds the best bundle for a bsn
		 */
		function getBundle(bsn) {
			var found;

			if ($scope.bundles) {
				for ( var i in $scope.bundles) {
					var b = $scope.bundles[i];
					if (b.bsn === bsn)
						if (!found
								|| compareVersions(b.version, found.version) > 0)
							found = b;
				}
			}
			return found;
		}

		/**
		 * Compare two version.
		 */
		function compareVersions(a, b) {
			if (a === b)
				return 0;
			if (a && !b)
				return 1;
			if (!a && b)
				return -1;

			var aa = a.split('\.');
			var bb = b.split('\.');
			for ( var i = 0; i < 3; i++) {
				var na = parseInt(aa[i]);
				var nb = parseInt(bb[i]);
				if (na != nb) {
					if (na > nb)
						return 1;
					else
						return -1;
				}
			}
			if (aa[3] && !bb[3])
				return 1;

			if (!aa[3] && bb[3])
				return -1;

			if (aa[3] > bb[3])
				return 1;
			if (aa[3] < bb[3])
				return -1;

			return 0;
		}

		/*
		 * Setup the scope. Add a number of variables and functions for the
		 * search.htm file
		 */
		$scope.query = $routeParams.query
		$scope.icon = function(program) {
			return (program.last.icon) || program.icon
					|| "https://jpm4j.org/img/default-icon.png";
		}

		function refresh(program) {
			window.location = "bundles/" + getBundle(program.last.bsn).bundleId;
		}
		$scope.manage = function(program) {
			if (program.groupId == 'osgi'
					&& program.artifactId == 'jpm4j.webconsole') {
				var audio = document.getElementById('dave');
				if (audio) {
					audio.volume = 0.5;
					audio.play();
					setTimeout(function() {
						refresh(program);
					}, (audio.duration+1)*1000);
				}
			} else
				refresh(program);

		}
		$scope.refresh = function() {
			send('REFRESH');
		}
		$scope.update = function(program) {
			send('UPDATE', program.last.url,
					getBundle(program.last.bsn).bundleId);
		}
		$scope.install = function(program) {
			send('INSTALL', program.last.url);
		}

		$scope.type = function(program) {
			var b = getBundle(program.last.bsn);
			if (!b)
				return 'install';

			if (compareVersions(program.last.version, b.version) > 0)
				return 'update';

			return 'manage';
		}

		$scope.search = function() {
			$location.url('/search/' + $scope.query);
		}
		$scope.me = function(program) {
			if (program.last.bsn === 'jpm4j.webconsole')
				return 'isme';
			else
				return '';
		}

		// Start function, everything before is just initialization

		send('FIRST')
	}

	function AqHelp() {

	}

	/**
	 * Create a module.
	 */
	angular.module('aq-console-plugins', []).config(function($routeProvider) {
		$routeProvider.when('/search/:query', {
			templateUrl : 'plugins/search.htm',
			controller : AqSearch
		}).when('/help', {
			templateUrl : 'plugins/help.htm',
			controller : AqHelp
		}).otherwise({
			redirectTo : '/search/'
		})
	});
})()
