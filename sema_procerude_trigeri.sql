go

CREATE DATABASE projekat_sab

GO

use projekat_sab

GO

CREATE TYPE [MyRealNumber]
	FROM DECIMAL(10,3) NULL
go

CREATE TYPE [MyString]
	FROM VARCHAR(100) NULL
go

CREATE DEFAULT [Default_nula]
	AS 0
go

CREATE RULE [TipGoriva0_1_2]
	AS @col IN (0, 1, 2)
go

CREATE RULE [VeceJednakoNula]
	AS @col >= 0
go

CREATE RULE [TipPaketa]
	AS @col IN (0, 1, 2, 3)
go

CREATE RULE [StatusIsporuke]
	AS @col IN (0, 1, 2, 3, 4)
go

CREATE RULE [VozacStatus]
	AS @col IN (0, 1)
go

CREATE TABLE [Administrator]
( 
	[idAdministrator]    integer  NOT NULL 
)
go

CREATE TABLE [Adresa]
( 
	[idAdresa]           integer  IDENTITY  NOT NULL ,
	[ulica]              [MyString]  NOT NULL ,
	[broj]               integer  NOT NULL ,
	[y]                  [MyRealNumber]  NOT NULL ,
	[idGrad]             integer  NOT NULL ,
	[x]                  [MyRealNumber]  NOT NULL 
)
go

CREATE TABLE [Grad]
( 
	[naziv]              [MyString]  NOT NULL ,
	[postanskiBroj]      integer  NOT NULL ,
	[idGrad]             integer  IDENTITY  NOT NULL 
)
go

CREATE TABLE [Isporuka]
( 
	[idDolaznaAdresa]    integer  NOT NULL ,
	[idPolaznaAdresa]    integer  NOT NULL ,
	[idIsporuka]         integer  IDENTITY  NOT NULL ,
	[tipPaketa]          integer  NOT NULL ,
	[tezinaPaketa]       [MyRealNumber]  NOT NULL ,
	[cena]               [MyRealNumber]  NOT NULL ,
	[status]             integer  NOT NULL ,
	[vremeKreiranja]     datetime  NOT NULL 
	CONSTRAINT [TrenutnoVreme]
		 DEFAULT  GETDATE(),
	[vremePrihvatanja]   datetime  NULL ,
	[idKorisnik]         integer  NOT NULL ,
	[idMagacin]          integer  NULL 
)
go

CREATE TABLE [Korisnik]
( 
	[idKorisnik]         integer  IDENTITY  NOT NULL ,
	[ime]                [MyString]  NOT NULL ,
	[prezime]            [MyString]  NOT NULL ,
	[korisnickoIme]      [MyString]  NOT NULL ,
	[sifra]              [MyString]  NOT NULL ,
	[idAdresa]           integer  NOT NULL 
)
go

CREATE TABLE [Kurir]
( 
	[idKurir]            integer  NOT NULL ,
	[brojIsporucenihPaketa] integer  NOT NULL ,
	[status]             integer  NOT NULL ,
	[profit]             [MyRealNumber]  NOT NULL ,
	[brojVozackeDozvole] [MyString]  NOT NULL 
)
go

CREATE TABLE [Magacin]
( 
	[idMagacin]          integer  NOT NULL ,
	[idGrad]             integer  NOT NULL 
)
go

CREATE TABLE [NekadaVozio]
( 
	[idKurir]            integer  NOT NULL ,
	[registracioniBroj]  [MyString]  NOT NULL 
)
go

CREATE TABLE [PreuzetaIsporuka]
( 
	[idVoznja]           integer  NULL ,
	[idIsporuka]         integer  NOT NULL 
)
go

CREATE TABLE [SlanjeIsporuke]
( 
	[idIsporuka]         integer  NOT NULL ,
	[idVoznja]           integer  NULL 
)
go

CREATE TABLE [Vozilo]
( 
	[registracioniBroj]  [MyString]  NOT NULL ,
	[tipGoriva]          integer  NOT NULL ,
	[potrosnja]          [MyRealNumber]  NOT NULL ,
	[nosivost]           [MyRealNumber]  NOT NULL ,
	[idMagacin]          integer  NULL 
)
go

CREATE TABLE [VoziTrenutno]
( 
	[registracioniBroj]  [MyString]  NOT NULL ,
	[idKurir]            integer  NOT NULL 
)
go

CREATE TABLE [Voznja]
( 
	[idVoznja]           integer  IDENTITY  NOT NULL ,
	[registracioniBroj]  [MyString]  NOT NULL ,
	[idKurir]            integer  NOT NULL ,
	[predjeniPut]        [MyRealNumber]  NOT NULL ,
	[opterecenostVozila] [MyRealNumber]  NOT NULL ,
	[prihod]             [MyRealNumber]  NOT NULL ,
	[idAdresa]           integer  NOT NULL ,
	[faza]               integer  NOT NULL ,
	[vremeKreiranja]     datetime  NOT NULL 
	CONSTRAINT [TrenutnoVreme_1801669503]
		 DEFAULT  GETDATE()
)
go

CREATE TABLE [ZahtevZaKurira]
( 
	[idKorisnik]         integer  NOT NULL ,
	[brojVozackeDozvole] [MyString]  NOT NULL 
)
go

ALTER TABLE [Administrator]
	ADD CONSTRAINT [XPKAdministrator] PRIMARY KEY  CLUSTERED ([idAdministrator] ASC)
go

ALTER TABLE [Adresa]
	ADD CONSTRAINT [XPKAdresa] PRIMARY KEY  CLUSTERED ([idAdresa] ASC)
go

ALTER TABLE [Grad]
	ADD CONSTRAINT [XPKGrad] PRIMARY KEY  CLUSTERED ([idGrad] ASC)
go

ALTER TABLE [Grad]
	ADD CONSTRAINT [XAK1Grad] UNIQUE ([postanskiBroj]  ASC)
go

ALTER TABLE [Isporuka]
	ADD CONSTRAINT [XPKIsporuka] PRIMARY KEY  CLUSTERED ([idIsporuka] ASC)
go


exec sp_bindefault '[Default_nula]', '[Isporuka].[cena]'
go

exec sp_bindefault '[Default_nula]', '[Isporuka].[status]'
go

ALTER TABLE [Korisnik]
	ADD CONSTRAINT [XPKKorisnik] PRIMARY KEY  CLUSTERED ([idKorisnik] ASC)
go

ALTER TABLE [Korisnik]
	ADD CONSTRAINT [XAK1Korisnik] UNIQUE ([korisnickoIme]  ASC)
go

ALTER TABLE [Kurir]
	ADD CONSTRAINT [XPKKurir] PRIMARY KEY  CLUSTERED ([idKurir] ASC)
go

ALTER TABLE [Kurir]
	ADD CONSTRAINT [XAK1Kurir] UNIQUE ([brojVozackeDozvole]  ASC)
go


exec sp_bindefault '[Default_nula]', '[Kurir].[brojIsporucenihPaketa]'
go

exec sp_bindefault '[Default_nula]', '[Kurir].[status]'
go

exec sp_bindefault '[Default_nula]', '[Kurir].[profit]'
go

ALTER TABLE [Magacin]
	ADD CONSTRAINT [XPKMagacin] PRIMARY KEY  CLUSTERED ([idMagacin] ASC)
go

ALTER TABLE [Magacin]
	ADD CONSTRAINT [XAK1Magacin] UNIQUE ([idGrad]  ASC)
go

ALTER TABLE [NekadaVozio]
	ADD CONSTRAINT [XPKNekadaVozio] PRIMARY KEY  CLUSTERED ([idKurir] ASC,[registracioniBroj] ASC)
go

ALTER TABLE [PreuzetaIsporuka]
	ADD CONSTRAINT [XPKPreuzetaIsporuka] PRIMARY KEY  CLUSTERED ([idIsporuka] ASC)
go

ALTER TABLE [SlanjeIsporuke]
	ADD CONSTRAINT [XPKSlanjeIsporuke] PRIMARY KEY  CLUSTERED ([idIsporuka] ASC)
go

ALTER TABLE [Vozilo]
	ADD CONSTRAINT [XPKVozilo] PRIMARY KEY  CLUSTERED ([registracioniBroj] ASC)
go

ALTER TABLE [VoziTrenutno]
	ADD CONSTRAINT [XPKVoziTrenutno] PRIMARY KEY  CLUSTERED ([registracioniBroj] ASC,[idKurir] ASC)
go

ALTER TABLE [Voznja]
	ADD CONSTRAINT [XPKVoznja] PRIMARY KEY  CLUSTERED ([idVoznja] ASC)
go


exec sp_bindefault '[Default_nula]', '[Voznja].[predjeniPut]'
go

exec sp_bindefault '[Default_nula]', '[Voznja].[opterecenostVozila]'
go

exec sp_bindefault '[Default_nula]', '[Voznja].[prihod]'
go

exec sp_bindefault '[Default_nula]', '[Voznja].[faza]'
go

ALTER TABLE [ZahtevZaKurira]
	ADD CONSTRAINT [XPKZahtevZaKurira] PRIMARY KEY  CLUSTERED ([idKorisnik] ASC)
go

ALTER TABLE [ZahtevZaKurira]
	ADD CONSTRAINT [XAK1ZahtevZaKurira] UNIQUE ([brojVozackeDozvole]  ASC)
go


ALTER TABLE [Administrator]
	ADD CONSTRAINT [is_a2] FOREIGN KEY ([idAdministrator]) REFERENCES [Korisnik]([idKorisnik])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


exec sp_bindrule '[VeceJednakoNula]', '[Adresa].[broj]'
go


ALTER TABLE [Adresa]
	ADD CONSTRAINT [R_2] FOREIGN KEY ([idGrad]) REFERENCES [Grad]([idGrad])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


exec sp_bindrule '[VeceJednakoNula]', '[Grad].[postanskiBroj]'
go


exec sp_bindrule '[TipPaketa]', '[Isporuka].[tipPaketa]'
go

exec sp_bindrule '[VeceJednakoNula]', '[Isporuka].[tezinaPaketa]'
go

exec sp_bindrule '[VeceJednakoNula]', '[Isporuka].[cena]'
go

exec sp_bindrule '[StatusIsporuke]', '[Isporuka].[status]'
go


ALTER TABLE [Isporuka]
	ADD CONSTRAINT [R_18] FOREIGN KEY ([idDolaznaAdresa]) REFERENCES [Adresa]([idAdresa])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Isporuka]
	ADD CONSTRAINT [R_19] FOREIGN KEY ([idPolaznaAdresa]) REFERENCES [Adresa]([idAdresa])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Isporuka]
	ADD CONSTRAINT [R_20] FOREIGN KEY ([idMagacin]) REFERENCES [Magacin]([idMagacin])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Isporuka]
	ADD CONSTRAINT [R_26] FOREIGN KEY ([idKorisnik]) REFERENCES [Korisnik]([idKorisnik])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Korisnik]
	ADD CONSTRAINT [R_21] FOREIGN KEY ([idAdresa]) REFERENCES [Adresa]([idAdresa])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


exec sp_bindrule '[VeceJednakoNula]', '[Kurir].[brojIsporucenihPaketa]'
go

exec sp_bindrule '[VozacStatus]', '[Kurir].[status]'
go

exec sp_bindrule '[VeceJednakoNula]', '[Kurir].[profit]'
go


ALTER TABLE [Kurir]
	ADD CONSTRAINT [is_a1] FOREIGN KEY ([idKurir]) REFERENCES [Korisnik]([idKorisnik])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Magacin]
	ADD CONSTRAINT [R_42] FOREIGN KEY ([idMagacin]) REFERENCES [Adresa]([idAdresa])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Magacin]
	ADD CONSTRAINT [R_45] FOREIGN KEY ([idGrad]) REFERENCES [Grad]([idGrad])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [NekadaVozio]
	ADD CONSTRAINT [R_24] FOREIGN KEY ([idKurir]) REFERENCES [Kurir]([idKurir])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [NekadaVozio]
	ADD CONSTRAINT [R_25] FOREIGN KEY ([registracioniBroj]) REFERENCES [Vozilo]([registracioniBroj])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [PreuzetaIsporuka]
	ADD CONSTRAINT [R_34] FOREIGN KEY ([idVoznja]) REFERENCES [Voznja]([idVoznja])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [PreuzetaIsporuka]
	ADD CONSTRAINT [R_35] FOREIGN KEY ([idIsporuka]) REFERENCES [Isporuka]([idIsporuka])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [SlanjeIsporuke]
	ADD CONSTRAINT [R_36] FOREIGN KEY ([idIsporuka]) REFERENCES [Isporuka]([idIsporuka])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [SlanjeIsporuke]
	ADD CONSTRAINT [R_37] FOREIGN KEY ([idVoznja]) REFERENCES [Voznja]([idVoznja])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


exec sp_bindrule '[TipGoriva0_1_2]', '[Vozilo].[tipGoriva]'
go

exec sp_bindrule '[VeceJednakoNula]', '[Vozilo].[potrosnja]'
go

exec sp_bindrule '[VeceJednakoNula]', '[Vozilo].[nosivost]'
go


ALTER TABLE [Vozilo]
	ADD CONSTRAINT [R_40] FOREIGN KEY ([idMagacin]) REFERENCES [Magacin]([idMagacin])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [VoziTrenutno]
	ADD CONSTRAINT [R_43] FOREIGN KEY ([registracioniBroj]) REFERENCES [Vozilo]([registracioniBroj])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [VoziTrenutno]
	ADD CONSTRAINT [R_44] FOREIGN KEY ([idKurir]) REFERENCES [Kurir]([idKurir])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


exec sp_bindrule '[VeceJednakoNula]', '[Voznja].[predjeniPut]'
go

exec sp_bindrule '[VeceJednakoNula]', '[Voznja].[opterecenostVozila]'
go


ALTER TABLE [Voznja]
	ADD CONSTRAINT [R_22] FOREIGN KEY ([registracioniBroj]) REFERENCES [Vozilo]([registracioniBroj])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Voznja]
	ADD CONSTRAINT [R_23] FOREIGN KEY ([idKurir]) REFERENCES [Kurir]([idKurir])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Voznja]
	ADD CONSTRAINT [R_41] FOREIGN KEY ([idAdresa]) REFERENCES [Adresa]([idAdresa])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

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
ALTER TABLE [ZahtevZaKurira]
	ADD CONSTRAINT [R_16] FOREIGN KEY ([idKorisnik]) REFERENCES [Korisnik]([idKorisnik])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go



