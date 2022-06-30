INSERT INTO oauth_client_details
	(client_id, client_secret, scope, authorized_grant_types,
	web_server_redirect_uri, authorities, access_token_validity,
	refresh_token_validity, additional_information, autoapprove)
VALUES
	('client1', 'secret', 'read,write,homepage',
	'password,authorization_code,refresh_token', null, null, 36000000, 36000000, null, true);
INSERT INTO oauth_client_details
	(client_id, client_secret, scope, authorized_grant_types,
	web_server_redirect_uri, authorities, access_token_validity,
	refresh_token_validity, additional_information, autoapprove)
VALUES
	('client2', 'secret', 'read,write,showbot',
	'password,authorization_code,refresh_token', null, null, 36000000, 36000000, null, true);
INSERT INTO oauth_client_details
	(client_id, client_secret, scope, authorized_grant_types,
	web_server_redirect_uri, authorities, access_token_validity,
	refresh_token_validity, additional_information, autoapprove)
VALUES
	('client3', 'secret', 'read,write,billing',
	'password,authorization_code,refresh_token', null, null, 36000000, 36000000, null, true);