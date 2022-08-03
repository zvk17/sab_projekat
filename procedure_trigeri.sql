go

use projekat_sab

go
create function fnDohvatiPocetnuCenu(@tipPaketa int)
returns decimal(10,3)
as
begin
	declare @pocetnaCena decimal(10,3);
	
	set @pocetnaCena = 0;
	if @tipPaketa = 0
	begin
		set @pocetnaCena = 115;
	end
	
	if @tipPaketa = 1
	begin
		set @pocetnaCena = 175;
	end
	
	if @tipPaketa = 2
	begin
		set @pocetnaCena = 250;
	end
	if @tipPaketa = 3
	begin
		set @pocetnaCena = 350;
	end
	
	return @pocetnaCena;
end

go

create function fnDohvatiCenuPoKg(@tipPaketa int)
returns decimal(10,3)
as
begin
	declare @cenaPoKg decimal(10,3);
	set @cenaPoKg = 0;
	if @tipPaketa = 0
	begin
		set @cenaPoKg = 0;
	end
	
	if @tipPaketa = 1
	begin
		set @cenaPoKg = 100;
	end
	
	if @tipPaketa = 2
	begin
		set @cenaPoKg = 100;
	end
	if @tipPaketa = 3
	begin
		set @cenaPoKg = 500;
	end
	return @cenaPoKg;
end

go

create function fnDohvatiCenuGorivaPoLitru(@tipGoriva int)
returns decimal(10,3)
as
begin
	declare @cenaPoLitru decimal(10,3);
	set @cenaPoLitru = 0;
	if @tipGoriva = 0
	begin
		set @cenaPoLitru = 15;
	end
	
	if @tipGoriva = 1
	begin
		set @cenaPoLitru = 36;
	end
	
	if @tipGoriva = 2
	begin
		set @cenaPoLitru = 32;
	end

	return @cenaPoLitru;
end
go

create function fnEuklidskaDistanca(@x1 decimal(10,3), @y1 decimal(10,3), @x2 decimal(10,3), @y2 decimal(10,3))
returns decimal(10,3)
as
begin
	declare @ret decimal(10,3);
	declare @deltaX decimal(10,3);
	declare @deltaY decimal(10,3);
	set @deltaX = @x1 - @x2;
	set @deltaY = @y1 - @y2;
	set @ret = @deltaX * @deltaX + @deltaY * @deltaY;
	set @ret = sqrt(@ret);
	return @ret;
end
go

create function fnIzracunajCenu(@idIsporuka int)
returns decimal(10,3)
as
begin
	declare @idPolaznaAdresa int;
	declare @idDolaznaAdresa int;
	declare @tipPaketa int;
	declare @tezinaPaketa decimal(10,3);
	
	SELECT  @tipPaketa = i.tipPaketa,
			@tezinaPaketa = i.tezinaPaketa, 
			@idPolaznaAdresa = i.idPolaznaAdresa,
			@idDolaznaAdresa = i.idDolaznaAdresa
	from Isporuka i
	WHERE i.idIsporuka = @idIsporuka;
	
	declare @x1 decimal(10,3);
	declare @y1 decimal(10,3);
	declare @x2 decimal(10,3);
	declare @y2 decimal(10,3);
	declare @ret decimal(10,3);
	
	SELECT @x1 = a.x, @y1 = a.y
	from Adresa a
	WHERE a.idAdresa = @idPolaznaAdresa;
	
	SELECT @x2 = a.x, @y2 = a.y
	from Adresa a
	WHERE a.idAdresa = @idDolaznaAdresa;
	
	set @ret = dbo.fnDohvatiPocetnuCenu(@tipPaketa) + dbo.fnDohvatiCenuPoKg(@tipPaketa) * @tezinaPaketa;
	set @ret = @ret * dbo.fnEuklidskaDistanca(@x1, @y1, @x2, @y2)

	return @ret;	
end
go

create procedure spAzurirajCenu @idIsporuka int
as
begin

	declare @cena decimal(10,3);	

	set @cena = dbo.fnIzracunajCenu(@idIsporuka);
	
	update Isporuka 
	SET cena = @cena
	WHERE idIsporuka = @idIsporuka
end
go
-- trigger za insert
create trigger TR_TransportOffer_After_Insert
    ON Isporuka
    AFTER INSERT
AS BEGIN
    declare @idIsporuka int;
    declare @kursor cursor;

    set @kursor = cursor for
    select idIsporuka
    from inserted;
	
	
	open @kursor;
    
    fetch next from @kursor
    into @idIsporuka;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        exec spAzurirajCenu @idIsporuka;
        

        fetch next from @kursor
        into @idIsporuka;
    end
	close @kursor
	deallocate @kursor
END
go
create trigger TR_TransportOffer_After_Update
	ON Isporuka
	AFTER UPDATE
AS
BEGIN
	declare @idIsporuka int;
    declare @kursor cursor;
	declare @status integer;

    set @kursor = cursor for
    select idIsporuka, status
    from deleted;
	
	open @kursor;
    
    fetch next from @kursor
    into @idIsporuka, @status;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        if UPDATE(idPolaznaAdresa) OR UPDATE(idDolaznaAdresa) OR UPDATE(tipPaketa) OR UPDATE(tezinaPaketa) OR UPDATE(cena)
		BEGIN
			if @status != 0
			BEGIN
				ROLLBACK TRANSACTION;
				RAISERROR ( 'Zabranjeno azuriranje Isporuke koja nije u nultom 0 - stanju', 1, 1)
				break
			END
		
		END
		EXEC spAzurirajCenu @idIsporuka;
        

        fetch next from @kursor
		into @idIsporuka, @status;
    end
	close @kursor
	deallocate @kursor

END


go
create TRIGGER VoziTrenutnoUpis
	ON VoziTrenutno
	AFTER INSERT
AS BEGIN
	declare @kursor cursor;
	declare @idKurir integer;
	declare @brojKurira integer;
	declare @brojVozila integer;
	declare @registracioniBroj varchar(100)


	set @kursor = cursor for
	select idKurir, registracioniBroj
	from inserted;

	open @kursor;
	fetch next from @kursor
	into @idKurir, @registracioniBroj;

	while @@FETCH_STATUS = 0
	begin
		select @brojKurira = COUNT(*)
		from VoziTrenutno vt
		WHERE vt.idKurir = @idKurir

		select @brojVozila = COUNT(*)
		from VoziTrenutno vt
		WHERE vt.registracioniBroj = @registracioniBroj

		if @brojKurira > 1
		begin
			ROLLBACK TRANSACTION;
			RAISERROR ( 'Broj kurira veci od 1', 1, 1)
			BREAK;
		end
		if @brojVozila > 1
		begin
		
			ROLLBACK TRANSACTION;
			RAISERROR ( 'Broj vozila veci od 1', 1, 2)
			BREAK;
		end

		fetch next from @kursor
		into @idKurir, @registracioniBroj;
	end
	close @kursor;
	deallocate @kursor;



END

GO

CREATE procedure spIsprazniBazu
AS BEGIN
	DELETE FROM VoziTrenutno     WHERE 1 = 1
	DELETE FROM ZahtevZaKurira   WHERE 1 = 1
	DELETE FROM Administrator    WHERE 1 = 1
	DELETE FROM SlanjeIsporuke   WHERE 1 = 1
	DELETE FROM PreuzetaIsporuka WHERE 1 = 1
	DELETE FROM Isporuka         WHERE 1 = 1
	DELETE FROM Voznja           WHERE 1 = 1
	DELETE FROM Vozilo           WHERE 1 = 1
	DELETE FROM Kurir            WHERE 1 = 1
	DELETE FROM Korisnik         WHERE 1 = 1
	DELETE FROM Magacin          WHERE 1 = 1
	DELETE FROM Adresa           WHERE 1 = 1
	DELETE FROM Grad             WHERE 1 = 1

END

GO

create TRIGGER ZahtevZaKurira_Vec_Je_Kurir
	ON ZahtevZaKurira
	AFTER INSERT
AS BEGIN
	declare @kursor cursor;
	declare @idKorisnik integer;
	declare @brojKurira integer;


	set @kursor = cursor for
	select idKorisnik
	from inserted;

	open @kursor;
	fetch next from @kursor
	into @idKorisnik;

	while @@FETCH_STATUS = 0
	begin
		select @brojKurira = COUNT(*)
		from Kurir k
		WHERE k.idKurir = @idKorisnik

		if @brojKurira > 0
		begin
			ROLLBACK TRANSACTION;
			RAISERROR ( 'Vec ima status kurira', 1, 1)
			BREAK;
		end
		

		fetch next from @kursor
		into @idKorisnik;
	end
	close @kursor;
	deallocate @kursor;



END

go

create TRIGGER Vozilo_Nije_Parkirano
	ON Vozilo
	AFTER UPDATE
AS BEGIN
	declare @registracioniBroj varchar(100);
    declare @kursor cursor;
	declare @idMagacin integer;

    set @kursor = cursor for
    select registracioniBroj, idMagacin
    from deleted;
	
	open @kursor;
    
    fetch next from @kursor
    into @registracioniBroj, @idMagacin;
	
	
    WHILE @@FETCH_STATUS = 0
    BEGIN
        if UPDATE([tipGoriva]) OR UPDATE([potrosnja]) OR UPDATE([nosivost])
		BEGIN
			if @idMagacin IS NULL
			BEGIN
				ROLLBACK TRANSACTION;
				RAISERROR ( 'Zabranjeno azuriranje Vozila koje nije parkirano', 1, 1)
				break
			END
		
		END	
        

        fetch next from @kursor
		into @registracioniBroj, @idMagacin;
    end
	close @kursor
	deallocate @kursor


END


GO
