const mensajes = require('../datos/mensajes.json');
const notificaciones = require('../datos/notificaciones.json');

const COLECCIONES_PUBLICAS = ['api::mensaje.mensaje', 'api::notificacion.notificacion'];
const ACCIONES_DE_LECTURA = ['find', 'findOne'];

async function sembrar(strapi, coleccion, campoClave, filas) {
  for (const fila of filas) {
    const existe = await strapi.documents(coleccion).findFirst({
      filters: { [campoClave]: fila[campoClave] },
    });
    if (!existe) {
      await strapi.documents(coleccion).create({ data: fila });
    }
  }
}

async function abrirLecturaPublica(strapi) {
  const rolPublico = await strapi.db
    .query('plugin::users-permissions.role')
    .findOne({ where: { type: 'public' } });

  if (!rolPublico) {
    return;
  }

  for (const coleccion of COLECCIONES_PUBLICAS) {
    for (const accion of ACCIONES_DE_LECTURA) {
      const nombre = `${coleccion}.${accion}`;
      const existe = await strapi.db
        .query('plugin::users-permissions.permission')
        .findOne({ where: { action: nombre, role: rolPublico.id } });
      if (!existe) {
        await strapi.db
          .query('plugin::users-permissions.permission')
          .create({ data: { action: nombre, role: rolPublico.id } });
      }
    }
  }
}

module.exports = {
  register() {},

  async bootstrap({ strapi }) {
    await sembrar(strapi, 'api::mensaje.mensaje', 'codigo', mensajes);
    await sembrar(strapi, 'api::notificacion.notificacion', 'canal', notificaciones);
    await abrirLecturaPublica(strapi);
  },
};
