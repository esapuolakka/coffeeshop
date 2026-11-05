-- Test data


INSERT INTO category (id, name, parent_category_id) VALUES (1, 'coffee-machines', 0) ON CONFLICT DO NOTHING;
INSERT INTO category (id, name, parent_category_id) VALUES (2, 'coffee-products', 0) ON CONFLICT DO NOTHING;
INSERT INTO category (id, name, parent_category_id) VALUES (3, 'espresso-machines', 1) ON CONFLICT DO NOTHING;
INSERT INTO category (id, name, parent_category_id) VALUES (4, 'filter-coffee', 1) ON CONFLICT DO NOTHING;
INSERT INTO category (id, name, parent_category_id) VALUES (5, 'coffee-grinders', 1) ON CONFLICT DO NOTHING;
INSERT INTO category (id, name, parent_category_id) VALUES (6, 'filters', 2) ON CONFLICT DO NOTHING;
INSERT INTO category (id, name, parent_category_id) VALUES (7, 'coffee', 2) ON CONFLICT DO NOTHING;
INSERT INTO category (id, name, parent_category_id) VALUES (8, 'espressos', 7) ON CONFLICT DO NOTHING;
INSERT INTO category (id, name, parent_category_id) VALUES (9, 'filter-coffees', 7) ON CONFLICT DO NOTHING;



INSERT INTO manufacturer (id, name, url) VALUES
(1, 'Siemens', 'https://www.siemens.com'),
(2, 'De’Longhi', 'https://www.delonghi.com'),
(3, 'Breville', 'https://www.breville.com'),
(4, 'Nespresso', 'https://www.nespresso.com'),
(5, 'Moka Pot', 'https://www.mokapot.com'),
(6, 'Hario', 'https://www.hario.com'),
(7, 'Bodum', 'https://www.bodum.com'),
(8, 'Aerolatte', 'https://www.aerolatte.com'),
(9, 'Baratza', 'https://www.baratza.com'),
(10, 'Keurig', 'https://www.keurig.com'),
(11, 'Gaggia', 'https://www.gaggia.com'),
(12, 'Café du Monde', 'https://www.cafedumonde.com'),
(13, 'Lavazza', 'https://www.lavazza.com'),
(14, 'Peet’s Coffee', 'https://www.peets.com'),
(15, 'Illy', 'https://www.illy.com'),
(16, 'Stumptown', 'https://www.stumptowncoffee.com'),
(17, 'Blue Bottle Coffee', 'https://www.bluebottlecoffee.com'),
(18, 'Peugeot', 'https://www.peugeot.com'),
(19, 'Café Puro', 'https://www.cafepuro.com')
ON CONFLICT DO NOTHING;



INSERT INTO supplier (id, name, contact_person, contact_person_email) VALUES
(1, 'Coffee Co', 'John Smith', 'john.smith@coffeeco.com'),
(2, 'Cafe Stores Ltd', 'Anna Taylor', 'anna.taylor@cafestores.fi'),
(3, 'Coffee Machines Finland', 'Peter Walker', 'peter.walker@coffeemachines.fi'),
(4, 'Cafe Experts', 'Tina Carter', 'tina.carter@cafeexperts.fi'),
(5, 'Coffee Products AB', 'Mike Brown', 'mike.brown@coffeeproductsab.com'),
(6, 'Coffee World', 'Sarah Reed', 'sarah.reed@coffeeworld.fi'),
(7, 'Coffee Lane', 'Victor Strong', 'victor.strong@coffeelane.fi'),
(8, 'Cafe & Pastries', 'Lisa Baker', 'lisa.baker@cafe-pastries.fi'),
(9, 'Coffee Wholesale', 'Jack Miller', 'jack.miller@coffeewholesale.fi'),
(10, 'Coffee Supplies', 'Sally Harper', 'sally.harper@coffeesupplies.fi')
ON CONFLICT DO NOTHING;



INSERT INTO item (id, name, description, price, image_id, category_id, supplier_id, manufacturer_id, created_at) VALUES 
(1, 'Siemens EQ.6 Plus S500 coffee machine', 'Automatic coffee machine with SensoFlow technology and OneTouch brewing.', 1299, NULL, 1, 1, 1, NOW()),
(2, 'De’Longhi Magnifica S ECAM 22.110', 'Automatic coffee machine for espresso and filter coffee with one-touch operation.', 699, NULL, 1, 2, 2, NOW()),
(3, 'Breville Barista Express', 'Coffee machine with integrated grinder for fresh beans and consistent quality.', 899, NULL, 1, 3, 1, NOW()),
(4, 'Rancilio Silvia', 'Classic espresso machine with professional performance for home use.', 749, NULL, 1, 2, 3, NOW()),
(5, 'Nespresso VertuoPlus', 'Capsule coffee machine for multiple cup sizes and quick brewing.', 249, NULL, 1, 4, 4, NOW()),
(6, 'Moka Pot', 'Traditional stovetop coffee maker for rich and full-bodied coffee.', 30, NULL, 1, 5, 5, NOW()),
(7, 'Hario V60', 'Popular pour-over dripper designed for clean and flavorful filter coffee.', 25, NULL, 1, 6, 6, NOW()),
(8, 'Bodum Chambord French Press', 'Elegant French press that delivers deep and balanced flavor.', 40, NULL, 1, 5, 7, NOW()),
(9, 'Aerolatte Milk Frother', 'Handy tool for creating creamy milk foam for coffee drinks.', 20, NULL, 1, 4, 6, NOW()),
(10, 'Baratza Encore coffee grinder', 'High-quality burr grinder with consistent and precise grinding.', 199, NULL, 1, 2, 1, NOW()),
(11, 'Keurig K-Elite', 'Convenient single-serve coffee maker for fast and easy brewing.', 169, NULL, 1, 3, 4, NOW()),
(12, 'Gaggia Classic Pro', 'Professional-level espresso machine designed for home baristas.', 599, NULL, 1, 2, 3, NOW()),
(13, 'Café du Monde Chicory Coffee', 'French-inspired coffee blend with chicory and a distinctive taste.', 15, NULL, 1, 1, 4, NOW()),
(14, 'Lavazza Super Crema', 'Balanced coffee blend with bold flavors and creamy texture.', 25, NULL, 1, 3, 2, NOW()),
(15, 'Peet’s Coffee Major Dickason’s Blend', 'Dark roast coffee blend with intense flavor and aroma.', 20, NULL, 1, 2, 5, NOW()),
(16, 'Illy Classico Espresso', 'Premium espresso with smooth taste and rich aroma.', 12, NULL, 1, 1, 6, NOW()),
(17, 'Stumptown Coffee Roasters Hair Bender', 'Complex coffee blend with fruity and spicy notes.', 18, NULL, 1, 3, 1, NOW()),
(18, 'Blue Bottle Coffee Single Origin', 'Carefully selected single-origin coffee roasted for clarity and sweetness.', 22, NULL, 1, 4, 4, NOW()),
(19, 'Peugeot Bistro coffee grinder', 'Classic coffee grinder that combines design and functionality.', 45, NULL, 1, 5, 7, NOW()),
(20, 'Café Puro Organic Coffee', 'Organic coffee product with excellent taste and sustainable sourcing.', 16, NULL, 1, 1, 5, NOW()),
(21, 'Breville BES870XL Barista Express', 'All-in-one espresso machine with built-in grinder for fresh coffee.', 749, NULL, 1, 1, 1, NOW()),
(22, 'Jura E8', 'Automatic coffee machine for premium espresso and milk-based drinks.', 1899, NULL, 1, 2, 2, NOW()),
(23, 'Krups XP4600', 'Compact espresso machine that delivers quality coffee at home.', 399, NULL, 1, 3, 1, NOW()),
(24, 'Gaggia Anima Prestige', 'Automatic coffee machine with milk frothing for easy lattes and cappuccinos.', 899, NULL, 1, 4, 3, NOW()),
(25, 'Rancilio Rocky', 'High-performance coffee grinder designed for espresso enthusiasts.', 379, NULL, 1, 5, 4, NOW()),
(26, 'De’Longhi EC680M Dedica', 'Stylish espresso machine with a slim design for small kitchens.', 349, NULL, 1, 2, 5, NOW()),
(27, 'Saeco PicoBaristo', 'Compact automatic coffee machine for excellent drinks at the touch of a button.', 1099, NULL, 1, 3, 2, NOW()),
(28, 'Nespresso Lattissima Pro', 'Innovative capsule machine for effortless milk coffee drinks.', 499, NULL, 1, 4, 1, NOW()),
(29, 'Hamilton Beach FlexBrew', 'Versatile coffee machine for capsules and ground filter coffee.', 99, NULL, 1, 5, 6, NOW()),
(30, 'Technivorm Cup-One', 'Compact brewer that makes one perfect cup at a time.', 99, NULL, 1, 1, 3, NOW()),
(31, 'Smeg ECF01', 'Retro-style espresso machine with modern brewing technology.', 499, NULL, 1, 2, 2, NOW()),
(32, 'Café Barista', 'Easy-to-use espresso machine for flavorful coffee drinks.', 199, NULL, 1, 3, 4, NOW()),
(33, 'Breville BNV250BLK1BUC1 Vertuo', 'Capsule coffee machine designed for different cup sizes.', 249, NULL, 1, 4, 1, NOW()),
(34, 'La Pavoni Europiccola', 'Classic lever espresso machine for traditional coffee preparation.', 799, NULL, 1, 2, 3, NOW()),
(35, 'Krups KM785D50', 'Programmable coffee maker for easy specialty coffee preparation.', 159, NULL, 1, 5, 4, NOW()),
(36, 'Cuisinart DCC-3200P1', 'Programmable coffee maker that brews up to 14 cups.', 99, NULL, 1, 1, 5, NOW()),
(37, 'BUNN Speed Brew', 'Fast and efficient coffee maker, ideal for busy mornings.', 129, NULL, 1, 2, 2, NOW()),
(38, 'Fellow Ode Brew Grinder', 'Premium coffee grinder designed for filter brewing methods.', 299, NULL, 1, 3, 1, NOW()),
(39, 'Breville BES990BSS Oracle Touch', 'Top-tier automatic coffee machine with advanced brewing control.', 2999, NULL, 1, 4, 3, NOW()),
(40, 'Melitta Caffeo CI', 'Automatic coffee machine with personalized drink profiles.', 1199, NULL, 1, 5, 4, NOW())
ON CONFLICT DO NOTHING;

INSERT INTO item (id, name, description, price, image_id, category_id, supplier_id, manufacturer_id, created_at) VALUES 
(41, 'Lavazza Qualità Rossa', 'Balanced and full-bodied filter coffee for everyday enjoyment.', 12.99, NULL, 2, 1, 1, NOW()),
(42, 'Illy Espresso Medium Roast', 'Smooth and aromatic espresso made from high-quality beans.', 14.99, NULL, 2, 2, 2, NOW()),
(43, 'Peet’s Coffee Major Dickason’s Blend', 'Dark roast blend with rich flavor and aroma.', 15.49, NULL, 2, 3, 1, NOW()),
(44, 'Koppi Coffee Light Roast', 'Light roast coffee that highlights delicate bean notes.', 12.99, NULL, 2, 4, 3, NOW()),
(45, 'Starbucks Pike Place Roast', 'Balanced and smooth filter coffee for daily use.', 11.99, NULL, 2, 5, 4, NOW()),
(46, 'Nespresso Arpeggio', 'Intense coffee capsule with deep flavor and aroma.', 0.89, NULL, 2, 1, 2, NOW()),
(47, 'Tchibo Cafissimo Classico', 'Classic coffee capsule with a balanced and smooth profile.', 0.79, NULL, 2, 2, 1, NOW()),
(48, 'Doi Tung Coffee', 'Sustainably produced coffee with natural bean aromas.', 9.99, NULL, 2, 3, 2, NOW()),
(49, 'Café du Monde Chicory Coffee', 'French-Italian style coffee blend flavored with chicory.', 11.49, NULL, 2, 4, 3, NOW()),
(50, 'Peet’s Coffee Espresso Forte', 'Intense espresso blend, perfect for milk-based drinks.', 14.99, NULL, 2, 5, 4, NOW()),
(51, 'Dunkin’ Donuts Original Blend', 'Balanced and flavorful filter coffee for everyday routines.', 10.99, NULL, 2, 1, 1, NOW()),
(52, 'Julius Meinl Wiener Mélange', 'Traditional Austrian coffee blend with a smooth taste.', 13.99, NULL, 2, 2, 2, NOW()),
(53, 'Café Puro Organic Coffee', 'Organic coffee with great flavor and sustainable sourcing.', 12.49, NULL, 2, 3, 1, NOW()),
(54, 'Lavazza Super Crema', 'Smooth and creamy coffee with a rich crema.', 14.99, NULL, 2, 4, 3, NOW()),
(55, 'Gevalia House Blend', 'Balanced filter coffee that is ideal for breakfast.', 11.49, NULL, 2, 5, 4, NOW()),
(56, 'McCafé Premium Roast', 'Well-balanced roast designed for daily coffee drinking.', 10.49, NULL, 2, 1, 1, NOW()),
(57, 'Death Wish Coffee', 'Strong dark roast coffee with high caffeine content.', 16.99, NULL, 2, 2, 2, NOW()),
(58, 'Kicking Horse Coffee', 'Dark roast coffee with bold flavor and intense aroma.', 15.99, NULL, 2, 3, 1, NOW()),
(59, 'Eight O’Clock Coffee Original', 'Classic coffee blend loved by customers for decades.', 10.99, NULL, 2, 4, 3, NOW()),
(60, 'Nespresso Intenso', 'Intense coffee capsule with deep and robust flavor.', 0.89, NULL, 2, 5, 4, NOW()),
(61, 'Keurig Green Mountain Breakfast Blend', 'Light and fresh blend that is perfect for mornings.', 10.49, NULL, 2, 1, 1, NOW()),
(62, 'Blue Bottle Coffee Single Origin', 'Carefully selected single-origin coffee with refined flavor.', 15.99, NULL, 2, 2, 2, NOW()),
(63, 'Stumptown Coffee Roasters Hair Bender', 'Complex blend with fruity and spicy flavor notes.', 18.99, NULL, 2, 3, 1, NOW()),
(64, 'Brewed Awakening Coffee', 'Freshly ground coffee offering rich and satisfying taste.', 11.49, NULL, 2, 4, 3, NOW()),
(65, 'Chicory Blend Coffee', 'Unique coffee blend with chicory for a distinct character.', 9.99, NULL, 2, 5, 4, NOW()),
(66, 'Peet’s Coffee House Blend', 'Filter coffee blend that is perfect for home brewing.', 12.99, NULL, 2, 1, 1, NOW()),
(67, 'Java Coffee Company', 'Fresh roasted coffee with deep and rich flavor.', 13.99, NULL, 2, 2, 2, NOW()),
(68, 'Coffee Bean & Tea Leaf', 'High-quality specialty roast coffee.', 14.49, NULL, 2, 3, 1, NOW()),
(69, 'Café Bustelo Espresso', 'Bold and flavorful espresso with Spanish character.', 10.99, NULL, 2, 4, 3, NOW()),
(70, 'Barista Prima Coffeehouse', 'Dark and full-bodied blend, ideal for espresso drinks.', 14.99, NULL, 2, 5, 4, NOW()),
(71, 'Sumatra Mandheling', 'Strong and earthy coffee for serious coffee lovers.', 15.99, NULL, 2, 1, 1, NOW()),
(72, 'Ethiopian Yirgacheffe', 'Aromatic and fruity coffee with distinctive tasting notes.', 16.49, NULL, 2, 2, 2, NOW()),
(73, 'Colombian Supremo', 'Balanced coffee with smooth and sweet notes.', 14.99, NULL, 2, 3, 1, NOW()),
(74, 'Kona Coffee', 'Aromatic premium coffee produced in Hawaii.', 24.99, NULL, 2, 4, 3, NOW()),
(75, 'Costa Rican Tarrazu', 'Bright and strong coffee that pairs well with breakfast.', 18.99, NULL, 2, 5, 4, NOW()),
(76, 'French Roast', 'Bold dark roast with an intense flavor profile.', 11.49, NULL, 2, 1, 1, NOW()),
(77, 'Mild Blend Coffee', 'Smooth and mild coffee for a softer cup.', 9.99, NULL, 2, 2, 2, NOW()),
(78, 'Tuscany Blend Coffee', 'Italian-style coffee blend full of flavor.', 15.99, NULL, 2, 3, 1, NOW()),
(79, 'Vanilla Flavored Coffee', 'Flavored coffee with sweet vanilla notes.', 12.99, NULL, 2, 4, 3, NOW()),
(80, 'Hazelnut Coffee', 'Flavored coffee with delicious nutty aroma.', 13.99, NULL, 2, 5, 4, NOW())
ON CONFLICT DO NOTHING;


INSERT INTO role (id, name) VALUES (1, 'ROLE_ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO role (id, name) VALUES (2, 'ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO role (id, name) VALUES (3, 'ROLE_VIP') ON CONFLICT DO NOTHING;

INSERT INTO discount (id, discount_percentage) VALUES (1, 10.0) ON CONFLICT DO NOTHING;



