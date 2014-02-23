################################################################################
# 95_webViewControl.pm
# Modul for FHEM
#
# Modul for communication between WebViewControl Android App and FHEM
#
# contributed by Dirk Hoffmann 01/2013
# $Id:
#
################################################################################

package main;
use strict;
use warnings;

#########################
# Forward declaration
sub webViewControl_Initialize($);		# Initialize
sub webViewControl_Define($$);			# define <name> WEBVIEWCONTROL
sub webViewControl_Undef($$);			# delete
sub webViewControl_modifyJsInclude();	# include js parts
sub webViewControl_Set($@);				# set
sub webViewControl_Get($@);				# get
sub webViewControl_Cgi();				# analyze and parse URL

#########################
# Global variables
my $fhemUrl = '/webviewcontrol' ;

my %sets = (
	'screenBrightness'	=> 'screenBrightness', # slider,1,1,100',
	'volume'			=> 'volume', # slider,1,1,100',	
	'keepScreenOn'		=> 'keepScreenOn',
	'toastMessage'		=> 'toastMessage',
	'reload'			=> 'reload',
	'audioPlay'			=> 'audioPlay',
	'audioStop'			=> 'audioStop',
	'ttsSay'			=> 'ttsSay',
	'voiceRec'			=> 'voiceRec',
);
	
my %gets = (
	'powerLevel'				=> 1,
	'powerPlugged'				=> 1,
	'voiceRecognitionLastError'	=> 1,
	'voiceRecognitionLastResult'=> 1,
);

my $FW_encoding="UTF-8";		 # like in FHEMWEB: encoding hardcoded

################################################################################
# Implements Initialize function
#
# @param	hash	$hash	hash of device addressed
#
################################################################################
sub webViewControl_Initialize($) {
	my ($hash) = @_;

	$hash->{DefFn}		= 'webViewControl_Define';
	$hash->{UndefFn}	= 'webViewControl_Undef';
	$hash->{SetFn}		= 'webViewControl_Set';
	$hash->{GetFn}		= 'webViewControl_Get';
	$hash->{AttrList}	= 'loglevel:0,1,2,3,4,5,6 model';

	# CGI
	$data{FWEXT}{$fhemUrl}{FUNC} = 'webViewControl_Cgi';
}

################################################################################
# Implements DefFn function
#
# @param	hash	$hash	hash of device addressed
# @param	string	$def	definition string
#
# @return	string
#
################################################################################
sub webViewControl_Define($$) {
	my ($hash, $def) = @_;
	my @a = split("[ \t][ \t]*", $def);

	my $name = $hash->{NAME};
	return "wrong syntax: define <name> WEBVIEWCONTROL APP-ID" if int(@a)!=3;

	$hash->{appID} = $a[2];
	$modules{webViewControl}{defptr}{$name} = $hash;									  

	webViewControl_modifyJsInclude();
	return undef;
}

#############################
sub webViewControl_Undef($$) {
	my ($hash, $name) = @_;
  
	delete($modules{webViewControl}{defptr}{$name});
	webViewControl_modifyJsInclude();

  return undef;
}

sub webViewControl_modifyJsInclude() {
	my @appsArray;
	foreach my $appName (keys %{ $modules{webViewControl}{defptr} } ) {
		push(@appsArray, $modules{webViewControl}{defptr}{$appName}->{appID} . ': \'' . $appName . '\'');
	}

	my $vars = 'var wvcDevices = {' . join(', ', @appsArray) . '}';
	
	$data{FWEXT}{$fhemUrl}{SCRIPT} = 'cordova-2.3.0.js"></script>' .
									 '<script type="text/javascript" src="/fhem/js/webviewcontrol.js"></script>' .
									 '<script type="text/javascript">' . $vars . '</script>' .
									 '<script type="text/javascript" charset="UTF-8';
}

###################################
sub webViewControl_Set($@) {
	my ($hash, @a) = @_;
	my $setArgs = join(' ', sort values %sets);
 	my $name = shift @a;

	if (int(@a) == 1 && $a[0] eq '?') {
		my %localSets = %sets;
		$localSets{screenBrightness}.=':slider,1,1,255';
		$localSets{volume}.=':slider,0,1,15';
		my $setArgs = join(' ', sort values %localSets);
		return $setArgs;	
	}

	if ((int(@a) < 1) || (!defined $sets{$a[0]}) ) {
		return 'Please specify one of following set value: ' . $setArgs;
	}

	if (! (($sets{$a[0]} eq 'reload') || ($sets{$a[0]} eq 'audioStop')) ) {
		if ($sets{$a[0]} eq 'toastMessage' && (int(@a)) < 2) {
			return 'Please input a text for toastMessage';

		} elsif ($sets{$a[0]} eq 'keepScreenOn') {
			if ($a[1] ne 'on' && $a[1] ne 'off') {
				return 'keepScreenOn needs on of off';
			} else {
				$a[1] = ($a[1] eq 'on') ? 'true' : 'false'; 
			}
			
		} elsif ($sets{$a[0]} eq 'screenBrightness' && (int($a[1]) < 1 || int($a[1]) > 255)) {
			return 'screenBrightness needs value from 1 to 255';

		} elsif ($sets{$a[0]} eq 'volume' && (int($a[1]) < 0 || int($a[1]) > 15)) {
			return 'volume needs value from 0 to 15';

		} elsif ($sets{$a[0]} eq 'audioPlay' && (int(@a)) < 2 ) {
			return 'Please input a url where Audio to play.';

		} elsif ($sets{$a[0]} eq 'ttsSay' && (int(@a)) < 2 ) {
			return 'Please input a text to say.';

		} elsif ($sets{$a[0]} eq 'voiceRec' && ($a[1] ne 'start' && $a[1] ne 'stop')) {
			return 'voiceRec must set to start or stop';
		}
	}
	
	my $v = join(' ', @a);

	$hash->{CHANGED}[0] = $v;
	$hash->{STATE} = $v;
	$hash->{lastCmd} = $v;
	$hash->{READINGS}{state}{TIME} = TimeNow();
	$hash->{READINGS}{state}{VAL} = $v;
   
	return undef;
}

###################################
sub webViewControl_Set2($@) {
	my ($hash, @a) = @_;
 	my $name = shift @a;

	my $setArgs = join(' ', sort keys %sets);

	if (int(@a) < 1) {
		return 'Please specify one of following set value: ' . $setArgs;
	}
	
	if (int(@a) == 1 && $a[0] eq '?') {
		return $setArgs;	
	}

	if (int(@a) < 2) {
		return 'Unknown argument for ' . $a[0];
	}

	my $v = join(" ", @a);
#  Log GetLogLevel($name,2), "dummy set $name $v";

	$hash->{CHANGED}[0] = $v;
	$hash->{STATE} = $v;
	$hash->{READINGS}{state}{TIME} = TimeNow();
	$hash->{READINGS}{state}{VAL} = $v;
   
#       Log 1, $t;
#return $t;
	return undef;
}






sub webViewControl_Get($@) {
	my ($hash, @a) = @_;

	return ('argument missing, usage is <attribute>') if(@a!=2);

	if(!$gets{$a[1]}) {
		return $a[1] . 'Not supported by get. Supported attributes: ' . join(' ', keys %gets) ;
	}

	my $retVal;
	if ($hash->{READINGS}{$a[1]}{VAL}) {
		$retVal = $hash->{READINGS}{$a[1]}{VAL};
	} else {
		$retVal = $a[1] . ' not yet set';
	}
	
	return $retVal;
}

##################
# Answer requests for webviewcontrol url for set some readings
sub webViewControl_Cgi() {
	my ($htmlarg) = @_;         #URL

	$htmlarg =~ s/^\///;

	my @htmlpart = ();
	@htmlpart = split("\\?", $htmlarg) if ($htmlarg);  #split URL by ? 

	if ($htmlpart[1]) {
		$htmlpart[1] =~ s,^[?/],,;
		
		my @states = ();
		my $name = undef;
		my %readings = ();
		my $timeNow		= TimeNow();
		
		foreach my $pv (split("&", $htmlpart[1])) {		#per each URL-section devided by &
			$pv =~ s/\+/ /g;
			$pv =~ s/%(..)/chr(hex($1))/ge;
			my ($p,$v) = split("=",$pv, 2);				#$p = parameter, $v = value
			$p =~ s/[\r]\n/\\\n/g;
			$v =~ s/[\r]\n/\\\n/g;
			
			if ($p eq 'id') {
				$name = $v;
			} else {
				$readings{$p}{TIME} = $timeNow;
				$readings{$p}{VAL} = $v;
				push(@states, $p . '=' . $v);
			}
		}

		if ($modules{webViewControl}{defptr}{$name}) {
			my $state = join(', ', @states);
#			$modules{webViewControl}{defptr}{$name}->{CHANGED}[0] = $state;
			$modules{webViewControl}{defptr}{$name}->{STATE} = $state;
			$modules{webViewControl}{defptr}{$name}->{READINGS}{state}{VAL} = $state;
			$modules{webViewControl}{defptr}{$name}->{READINGS}{state}{TIME} = $timeNow;

			my $cc = 0;
			foreach my $reading (keys %readings) {
				$modules{webViewControl}{defptr}{$name}->{CHANGED}[$cc] = $reading . ': ' . $readings{$reading}{VAL};
				$modules{webViewControl}{defptr}{$name}->{READINGS}{$reading} = $readings{$reading};
				$cc++;
			}

			DoTrigger($name, undef);
		}
	}

	return ("text/html; charset=$FW_encoding", $FW_RET);	# $FW_RET composed by FW_pO, FP_pH etc
}

1;
